package saros.ui.wizards;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import saros.filesystem.EclipseReferencePoint;
import saros.filesystem.IReferencePoint;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;
import saros.ui.views.SarosView;
import saros.ui.wizards.pages.ResourceSelectionWizardPage;

/**
 * Wizard for adding resources to a running session.
 *
 * @author bkahlert
 */
public class AddResourcesToSessionWizard extends Wizard {
  public static final String TITLE = Messages.SessionAddProjectsWizard_title;

  private final ResourceSelectionWizardPage resourceSelectionWizardPage;

  /** @param preselectedResources resources that should be preselected or <code>null</code> */
  public AddResourcesToSessionWizard(final Collection<IResource> preselectedResources) {
    setWindowTitle(TITLE);
    setDefaultPageImageDescriptor(
        ImageManager.getImageDescriptor(ImageManager.WIZBAN_SESSION_ADD_PROJECTS));
    setHelpAvailable(false);
    resourceSelectionWizardPage = new ResourceSelectionWizardPage(preselectedResources);
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    SarosView.clearNotifications();
    return super.getNextPage(page);
  }

  @Override
  public void addPages() {
    addPage(resourceSelectionWizardPage);
  }

  @Override
  public boolean performFinish() {
    List<IContainer> selectedResources = resourceSelectionWizardPage.getSelectedResources();

    if (selectedResources == null || selectedResources.isEmpty()) return false;

    resourceSelectionWizardPage.rememberCurrentSelection();

    SarosView.clearNotifications();

    Set<IReferencePoint> referencePoints =
        selectedResources.stream().map(EclipseReferencePoint::new).collect(Collectors.toSet());

    CollaborationUtils.addReferencePointsToSession(referencePoints);

    return true;
  }
}
