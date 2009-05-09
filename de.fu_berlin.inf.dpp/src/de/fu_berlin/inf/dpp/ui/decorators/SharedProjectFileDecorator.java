/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.decorators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Decorates Shared Project files.
 * 
 * TODO CO SharedProjectFileDecorator does support multiple users but the
 * awareness shows all drivers and the person followed which is kind of
 * confusing.
 * 
 * @see ILightweightLabelDecorator *
 */
public class SharedProjectFileDecorator implements ILightweightLabelDecorator {

    private static final Logger log = Logger
        .getLogger(SharedProjectFileDecorator.class.getName());

    public final ImageDescriptor activeDescriptor = SarosUI
        .getImageDescriptor("icons/bullet_green.png"); // NON-NLS-1

    public final ImageDescriptor passiveDescriptor = SarosUI
        .getImageDescriptor("icons/bullet_yellow.png"); // NON-NLS-1

    protected ISharedProject sharedProject;

    protected Set<Object> decoratedElements;

    protected List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    /**
     * SharedProjectListener responsible for triggering an update on the
     * decorations if there is a role change.
     */
    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user, boolean replicated) {

            updateDecorations(user);
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            project.addListener(projectListener);

            if (!decoratedElements.isEmpty()) {
                log
                    .warn("Set of files to decorate not empty on session start. "
                        + decoratedElements.toString());
                // update remaining files
                updateDecoratorsAsync(decoratedElements.toArray());
            }
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject = null;
            project.removeListener(projectListener);
            // Update all
            updateDecoratorsAsync(decoratedElements.toArray());
        }
    };

    protected ISharedEditorListener editorListener = new ISharedEditorListener() {

        Map<User, IFile> oldActiveEditors = new HashMap<User, IFile>();

        public void activeEditorChanged(User user, IPath path) {
            try {
                List<IFile> paths = new LinkedList<IFile>();

                IFile oldActiveEditor = oldActiveEditors.get(user);
                if (oldActiveEditor != null) {
                    paths.add(oldActiveEditor);
                }

                IFile newFile = null;
                if (path != null && sharedProject != null) {
                    newFile = sharedProject.getProject().getFile(path);
                    if (newFile.exists() && !newFile.equals(oldActiveEditor)) {
                        paths.add(newFile);
                    }
                }
                oldActiveEditors.put(user, newFile);
                updateDecoratorsAsync(paths.toArray());

            } catch (RuntimeException e) {
                log.error("Internal Error in SharedProjectFileDecorator:", e);
            }
        }

        public void editorRemoved(User user, IPath path) {
            try {
                if (path != null && sharedProject != null) {
                    IFile newFile = sharedProject.getProject().getFile(path);
                    IFile oldActiveEditor = oldActiveEditors.get(user);
                    if (newFile.exists()) {
                        if (newFile.equals(oldActiveEditor)) {
                            oldActiveEditors.put(user, null);
                        }
                        updateDecoratorsAsync(new Object[] { newFile });
                    }
                }
            } catch (RuntimeException e) {
                log.error("Internal Error in SharedProjectFileDecorator:", e);
            }
        }

        public void driverEditorSaved(IPath path, boolean replicated) {
            // ignore
        }

        User oldUserFollowed = null;

        public void followModeChanged(User user) {

            if (ObjectUtils.equals(user, oldUserFollowed))
                return;

            if (oldUserFollowed != null) {
                updateDecorations(oldUserFollowed);
            }
            oldUserFollowed = user;
            if (user != null) {
                updateDecorations(user);
            }

        }
    };

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected ISessionManager sessionManager;

    public SharedProjectFileDecorator() {

        Saros.reinject(this);

        this.decoratedElements = new HashSet<Object>();

        sessionManager.addSessionListener(sessionListener);

        editorManager.addSharedEditorListener(editorListener);
        if (sessionManager.getSharedProject() != null) {
            sessionListener.sessionStarted(sessionManager.getSharedProject());
        }
    }

    protected void updateDecorations(User user) {
        if (sharedProject == null)
            return;

        List<IFile> files = new ArrayList<IFile>();

        for (IPath path : editorManager.getRemoteOpenEditors(user)) {
            IFile openFile = sharedProject.getProject().getFile(path);
            if (openFile.exists())
                files.add(openFile);
        }
        updateDecoratorsAsync(files.toArray());
    }

    public void decorate(Object element, IDecoration decoration) {
        if (decorateInternal(element, decoration)) {
            decoratedElements.add(element);
        } else {
            decoratedElements.remove(element);
        }
    }

    private boolean decorateInternal(Object element, IDecoration decoration) {
        try {
            if (this.sharedProject == null)
                return false;

            // Enablement in the Plugin.xml ensures that we only get IFiles
            if (!(element instanceof IFile))
                return false;

            IFile file = (IFile) element;
            if (!this.sharedProject.getProject().equals(file.getProject())) {
                return false;
            }
            IPath path = file.getProjectRelativePath();
            if (path == null)
                return false;

            if (containsUserToDisplay(editorManager
                .getRemoteActiveEditorUsers(path))) {
                log.trace("Active Deco: " + element);
                decoration.addOverlay(activeDescriptor, IDecoration.TOP_LEFT);
                return true;
            }
            if (containsUserToDisplay(editorManager
                .getRemoteOpenEditorUsers(path))) {
                log.trace("Passive Deco: " + element);
                decoration.addOverlay(passiveDescriptor, IDecoration.TOP_LEFT);
                return true;
            }

            log.trace("No Deco: " + element);

        } catch (RuntimeException e) {
            log.error("Internal Error in SharedProjectFileDecorator:", e);
        }
        return false;
    }

    protected boolean containsUserToDisplay(List<User> activeUsers) {

        for (User user : activeUsers) {
            if (user.isDriver() || user.equals(editorManager.getFollowedUser())) {
                return true;
            }
        }
        return false;
    }

    public void addListener(ILabelProviderListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ILabelProviderListener listener) {
        this.listeners.remove(listener);
    }

    public void dispose() {
        sessionManager.removeSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
        // TODO clean up better
        this.sharedProject = null;
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    protected void updateDecoratorsAsync(final Object[] updateElements) {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                LabelProviderChangedEvent event = new LabelProviderChangedEvent(
                    SharedProjectFileDecorator.this, updateElements);

                for (ILabelProviderListener listener : SharedProjectFileDecorator.this.listeners) {
                    listener.labelProviderChanged(event);
                }
            }
        });
    }
}