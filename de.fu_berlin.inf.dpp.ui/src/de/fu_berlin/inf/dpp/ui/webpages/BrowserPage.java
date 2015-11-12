package de.fu_berlin.inf.dpp.ui.webpages;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

/**
 * Abstract implementation of {@link IBrowserPage} which offers convenience
 * methods for registering browser functions and renderer.
 */
public abstract class BrowserPage implements IBrowserPage {

    /**
     * Common HTML document location
     */
    private static final String PATH = "html/dist/";
    private String relativePageLocation;

    /**
     * The title is shown to the user in the dialog.
     */
    private String pageTitle;

    private final List<JavascriptFunction> browserFunctions = new ArrayList<JavascriptFunction>();
    private final List<Renderer> renderers = new ArrayList<Renderer>();

    /**
     * Creates a new BrowserPage that encapsulates the location and title of the
     * HTML page as well as the needed browsers functions and renderer.
     * 
     * @param htmlDocName
     *            the file name of the HTML document without any path addition
     * @param pageTitle
     *            the title that will be shown in the dialog
     */
    public BrowserPage(String htmlDocName, String pageTitle) {
        this.relativePageLocation = PATH + htmlDocName;
        this.pageTitle = pageTitle;
    }

    // TODO: The prefix WEB is misleading, because there is no "WEB" involved
    // here. Eliminate all usage of "web" in naming inside the UI project
    @Override
    public String getWebpageResource() {
        return relativePageLocation;
    }

    @Override
    public String getTitle() {
        return pageTitle;
    }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return browserFunctions;
    }

    @Override
    public List<Renderer> getRenderers() {
        return renderers;
    }

    protected void addBrowserFunctions(JavascriptFunction... browserfunctions) {
        for (JavascriptFunction function : browserfunctions) {
            this.browserFunctions.add(function);
        }
    }

    protected void addRenderer(Renderer... renderers) {
        for (Renderer renderer : renderers) {
            this.renderers.add(renderer);
        }
    }
}
