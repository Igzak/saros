package de.fu_berlin.inf.dpp.ui.ide_embedding;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.webpages.IBrowserPage;

/**
 * IDE-independent base class for managing HTML dialogs.
 * 
 * Those dialogs are displayed in a new window inside a browser. The
 * simultaneous display of multiple dialogs is supported. However, there may
 * only be one dialog open for each webpage at the same time.
 */
public abstract class DialogManager {

    private static final Logger LOG = Logger.getLogger(DialogManager.class);

    private Map<String, IBrowserDialog> openDialogs = new HashMap<String, IBrowserDialog>();

    private final UISynchronizer uiSynchronizer;

    public DialogManager(UISynchronizer uiSynchronizer) {
        this.uiSynchronizer = uiSynchronizer;
    }

    /**
     * Shows a dialog displaying an HTML page. For each page there may only be
     * one open dialog window. If this method is called when the dialog is
     * already displayed, nothing happens.
     * <p/>
     * May be called from any thread.
     * 
     * @param browserPage
     *            the BrowserPage object to be displayed in the dialog
     */
    public void showDialogWindow(final IBrowserPage browserPage) {
        uiSynchronizer.asyncExec(new Runnable() {
            @Override
            public void run() {
                String webpageResource = browserPage.getWebpageResource();

                if (dialogIsOpen(webpageResource)) {
                    // If the user try to open a dialog that is already open,
                    // the dialog should get active and in the foreground to
                    // help the user find it.

                    reopenDialogWindow(webpageResource);
                    return;
                }

                IBrowserDialog dialog = createDialog(browserPage);

                openDialogs.put(webpageResource, dialog);
            }
        });
    }

    /**
     * This method is to overwritten to implement the IDE-specific opening of
     * the dialog. This method must only be called on the UI thread.
     * 
     * @param browserPage
     *            the BrowserPage object to be displayed in the dialog
     * @return an IDE-independent representation of the dialog
     */
    protected abstract IBrowserDialog createDialog(IBrowserPage browserPage);

    /**
     * Closes the dialog displaying the given page.
     * <p/>
     * May be called from any thread.
     * 
     * @param webPage
     *            a String representing the page, this string can be obtained
     *            via {@link IBrowserPage#getWebpageResource()}
     */
    public void closeDialogWindow(final String webPage) {
        uiSynchronizer.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!dialogIsOpen(webPage)) {
                    LOG.warn(webPage + "could not be found");
                    return;
                }

                // shell is removed in the ShellLister
                openDialogs.get(webPage).close();
            }
        });
    }

    /**
     * Set the location of a given dialog to the center of the eclipse instance.
     * If the given browserPage is not currently displayed in a shell/dialog
     * this does nothing.
     * 
     * @param webPage
     *            a String representing the page, this string can be obtained
     *            via {@link IBrowserPage#getWebpageResource()}
     */
    private void reopenDialogWindow(String webPage) {
        if (!dialogIsOpen(webPage)) {
            LOG.warn(webPage + "could not be found");
            return;
        }

        IBrowserDialog dialog = openDialogs.get(webPage);
        dialog.reopen();
    }

    /**
     * @param webPage
     *            a String representing the page, this string can be obtained
     *            via {@link IBrowserPage#getWebpageResource()}
     * @return true if the browserPage is currently displayed in a shell/dialog
     */
    private boolean dialogIsOpen(String webPage) {
        return openDialogs.containsKey(webPage);
    }

    /**
     * This method should be called in the IDE-specific close listeners to
     * remove the entry for the dialog.
     * 
     * @param webPage
     *            a String representing the page, this string can be obtained
     *            via {@link IBrowserPage#getWebpageResource()}
     */
    protected void removeDialogEntry(String webPage) {
        LOG.debug(webPage + " is closed");
        openDialogs.remove(webPage);
    }
}
