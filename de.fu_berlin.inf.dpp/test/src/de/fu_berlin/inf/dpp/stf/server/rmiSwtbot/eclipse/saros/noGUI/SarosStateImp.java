package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.limewire.collection.Tuple;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.StateImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponentImp;

/**
 * The goal of this class is to gather state and provide an RMI interface for
 * getting internal states from the outside.
 */
public class SarosStateImp extends StateImp implements SarosState {

    private JID jid;
    private transient static final Logger log = Logger
        .getLogger(SarosStateImp.class);

    private static transient SarosStateImp self;

    protected transient Saros saros;

    protected transient SarosSessionManager sessionManager;

    protected transient DataTransferManager dataTransferManager;

    protected transient EditorManager editorManager;

    protected transient XMPPAccountStore xmppAccountStore;

    /**
     * {@link ChatViewComponentImp} is a singleton, but inheritance is possible.
     */
    public static SarosStateImp getInstance(Saros saros,
        SarosSessionManager sessionManager,
        DataTransferManager dataTransferManager, EditorManager editorManager,
        XMPPAccountStore xmppAccountStore) {
        if (self != null)
            return self;
        self = new SarosStateImp(saros, sessionManager, dataTransferManager,
            editorManager, xmppAccountStore);
        return self;
    }

    public SarosStateImp(Saros saros, SarosSessionManager sessionManager,
        DataTransferManager dataTransferManager, EditorManager editorManager,
        XMPPAccountStore xmppAccountStore) {
        this.saros = saros;
        this.sessionManager = sessionManager;
        this.dataTransferManager = dataTransferManager;
        this.editorManager = editorManager;
        this.xmppAccountStore = xmppAccountStore;
        // this.messageManager = messageManger;
    }

    public boolean areDrivers(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISarosSession sarosSession = sessionManager.getSarosSession();

                User user = sarosSession.getUser(jid);
                result &= sarosSession.getDrivers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isExclusiveDriver() throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            return sarosSession.isExclusiveDriver();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areObservers(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISarosSession sarosSession = sessionManager.getSarosSession();
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getObservers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean areParticipants(List<JID> jids) {
        boolean result = true;
        for (JID jid : jids) {
            try {
                ISarosSession sarosSession = sessionManager.getSarosSession();
                result &= sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isInSession() {
        log.debug("isInSession() == " + sessionManager.getSarosSession() != null);
        return sessionManager.getSarosSession() != null;
    }

    public Tuple<NetTransferMode, NetTransferMode> getConnection(JID destJid) {
        NetTransferMode outgoingMode = dataTransferManager
            .getTransferMode(destJid);
        NetTransferMode incomingMode = dataTransferManager
            .getTransferMode(destJid);
        return new Tuple<NetTransferMode, NetTransferMode>(incomingMode,
            outgoingMode);
    }

    public ConnectionState getXmppConnectionState() {
        return saros.getConnectionState();
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        Roster roster = saros.getRoster();
        String jidBase = jid.getBase();
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            log.debug("roster entry'name : " + entry.getName());
            log.debug("roster entry'name : " + entry.getUser());
            log.debug("roster entry'name : " + entry.getStatus());
            log.debug("roster entry'name : " + entry.getType());
        }
        return roster.contains(jidBase);
    }

    public boolean isConnected() {
        return saros.isConnected();
    }

    public boolean isInFollowMode() throws RemoteException {
        return editorManager.isFollowing();
    }

    public boolean isFollowingUser(String plainJID) throws RemoteException {
        if (getFollowedUserJID() == null)
            return false;
        else
            return getFollowedUserJID().getBase().equals(plainJID);
    }

    public JID getFollowedUserJID() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    public boolean isDriver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        User user = sarosSession.getUser(jid);
        log.debug("isDriver(" + jid.toString() + ") == "
            + sarosSession.getDrivers().contains(user));
        return sarosSession.getDrivers().contains(user);
    }

    public boolean isDriver() throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        User user = sarosSession.getUser(this.jid);
        if (user == null) {
            // TODO here need to throw Exception.
            return false;
        }
        boolean isDriver = sarosSession.getDrivers().contains(user);
        log.debug("isDriver(" + this.jid.toString() + ") == " + isDriver);
        return isDriver;
    }

    public boolean isHost(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        final boolean result = sarosSession.getUser(jid) == sarosSession
            .getHost();
        log.debug("isHost(" + jid.toString() + ") == " + result);
        return result;
    }

    public boolean isIncomingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isIncomingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isIncomingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    // public boolean isClassDirty(String projectName, String pkg, String
    // className)
    // throws RemoteException, FileNotFoundException {
    // IPath path = new Path(projectName + "/src/"
    // + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
    // IResource resource = ResourcesPlugin.getWorkspace().getRoot()
    // .findMember(path);
    // return editorManager.isDirty(new SPath(resource));
    // }

    public boolean isIncomingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isObserver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        User user = sarosSession.getUser(jid);
        log.debug("isObserver(" + jid.toString() + ") == "
            + sarosSession.getObservers().contains(user));
        return sarosSession.getObservers().contains(user);
    }

    public boolean isOutgoingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isOutgoingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isOutgoingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isOutgoingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            log.debug("isParticipant("
                + jid.toString()
                + ") == "
                + sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid)));
            return sarosSession.getParticipants().contains(
                sarosSession.getUser(jid));
        } catch (Exception e) {
            return false;
        }
    }

    public ISarosSession getProject() throws RemoteException {
        return sessionManager.getSarosSession();
    }

    public String getContents(String path) throws RemoteException {
        Bundle bundle = saros.getBundle();
        String contents;
        try {
            contents = FileUtils.read(bundle.getEntry(path));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + path);
        }
        return contents;
    }

    public String getPathToScreenShot() throws RemoteException {
        Bundle bundle = saros.getBundle();
        return bundle.getLocation().substring(16) + SCREENSHOTDIR;
    }

    public void setJID(JID jid) throws RemoteException {
        this.jid = jid;
    }

    /**
     * TODO this method return null value... need to be fixed
     */
    public JID getJID() throws RemoteException {
        final JID result = saros.getMyJID();
        return result;
    }

    public boolean isSameUser(JID otherJID) throws RemoteException {
        return this.jid.equals(otherJID);
    }

    public boolean isAccountExist(JID jid) throws RemoteException {
        ArrayList<XMPPAccount> allAccounts = xmppAccountStore.getAllAccounts();
        for (XMPPAccount account : allAccounts) {
            log.debug("account id: " + account.getId());
            log.debug("account username: " + account.getUsername());
            log.debug("account password: " + account.getPassword());
            log.debug("account server: " + account.getServer());
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())) {
                return true;
            }
        }
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        return account.isActive();
    }

    public void activateAccount(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        xmppAccountStore.setAccountActive(account);
    }

    public void createAccount(String username, String password, String server)
        throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException {
        xmppAccountStore.changeAccountData(getXMPPAccount(jid).getId(),
            newUserName, newPassword, newServer);
    }

    public void deleteAccount(JID jid) throws RemoteException {
        xmppAccountStore.deleteAccount(getXMPPAccount(jid));
    }

    private XMPPAccount getXMPPAccount(JID id) {
        ArrayList<XMPPAccount> allAccounts = xmppAccountStore.getAllAccounts();
        for (XMPPAccount account : allAccounts) {
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())) {
                return account;
            }
        }
        return null;
    }
}
