package core.web;

import core.Environment;
import core.driver.DriverFactory;
import core.web.annotations.PageURL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.StringUtil;
import utils.logging.iLogger;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class iPage {
    private static final String MISSING_PAGE_URL_ANNOTATION = "Page URL is not specified in @RelativeURL annotation for class ";
    private static final ThreadLocal<PageInitializationContext> PAGE_INITIALIZATION_CONTEXT = new ThreadLocal<>();
    protected WebDriver driver;
    protected WebDriverWait wait;
    private String pageName;
    private iWebElement scopeRoot;

    public iPage() {
        PageInitializationContext initContext = PAGE_INITIALIZATION_CONTEXT.get();
        if (initContext != null) {
            initializePage(initContext.driver(), initContext.pageName(), initContext.scopeRoot());
        } else {
            initializePage(DriverFactory.getCurrentDriver(), getClass().getSimpleName(), null);
        }
    }

    protected final void initializePage(WebDriver driver, String pageName) {
        initializePage(driver, pageName, null);
    }

    protected final void initializePage(WebDriver driver, String pageName, iWebElement scopeRoot) {
        this.driver = driver;
        this.pageName = pageName;
        this.scopeRoot = scopeRoot;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        iPageFactory.initElements(this.driver, this);
    }

    static void beginPageInitialization(WebDriver driver, String pageName) {
        beginPageInitialization(driver, pageName, null);
    }

    static void beginPageInitialization(WebDriver driver, String pageName, iWebElement scopeRoot) {
        PAGE_INITIALIZATION_CONTEXT.set(new PageInitializationContext(driver, pageName, scopeRoot));
    }

    static void clearPageInitialization() {
        PAGE_INITIALIZATION_CONTEXT.remove();
    }

    iWebElement getScopeRoot() {
        return scopeRoot;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void openPage() {
        String absoluteUrl = getExpectedPageUrl();
        iLogger.info("Go to page " + pageName + " with absolute URL " + absoluteUrl);
        navigateToUrl(absoluteUrl);
    }

    /**
     * Returns the fully resolved page URL assembled from the current class {@link PageURL} hierarchy.
     */
    protected final String getExpectedPageUrl() {
        Class<?> pageClass = getClass();
        List<String> relativeSegments = new ArrayList<>();
        StringBuilder relativeUrl = new StringBuilder();
        do {
            PageURL annotation = pageClass.getAnnotation(PageURL.class);
            if (annotation != null) {
                String pageUrlValue = annotation.value();
                if (pageUrlValue.startsWith("http")) {
                    return buildAbsoluteUrl(pageUrlValue, relativeSegments);
                }
                String formattedRelativeUrl = StringUtil.formatRelativeURL(pageUrlValue);
                relativeSegments.add(0, formattedRelativeUrl);
                relativeUrl.insert(0, formattedRelativeUrl);
            } else
                throw new RuntimeException(MISSING_PAGE_URL_ANNOTATION + pageClass.getName()
                        + " or its parent");
            pageClass = pageClass.getSuperclass();
        } while (pageClass != iPage.class);

        return Environment.getRootUrl() + relativeUrl;
    }

    /**
     * Default open-state check based on the resolved {@link PageURL}.
     * Override when the page also needs a landmark or readiness check.
     */
    public boolean isOpened() {
        return urlsRepresentSamePage(getCurrentUrl(), getExpectedPageUrl());
    }

    private void navigateToUrl(String url) {
        try {
            URL validatedUrl = new URI(url).toURL();
            driver.get(validatedUrl.toString());
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }

    private static String buildAbsoluteUrl(String absoluteRootUrl, List<String> relativeSegments) {
        return StringUtil.cutExtraEndSlashes(absoluteRootUrl) + String.join("", relativeSegments);
    }

    private static boolean urlsRepresentSamePage(String currentUrl, String expectedUrl) {
        try {
            URI current = new URI(currentUrl);
            URI expected = new URI(expectedUrl);

            boolean hasExpectedQuery = expected.getQuery() != null && !expected.getQuery().isBlank();
            boolean queryMatches = !hasExpectedQuery || Objects.equals(current.getQuery(), expected.getQuery());

            return Objects.equals(current.getScheme(), expected.getScheme())
                    && Objects.equals(current.getHost(), expected.getHost())
                    && current.getPort() == expected.getPort()
                    && Objects.equals(normalizePath(current.getPath()), normalizePath(expected.getPath()))
                    && queryMatches;
        } catch (Exception ignored) {
            return normalizeUrlForComparison(currentUrl).equals(normalizeUrlForComparison(expectedUrl));
        }
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "/";
        }
        return StringUtil.cutExtraEndSlashes(path);
    }

    private static String normalizeUrlForComparison(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return StringUtil.cutExtraEndSlashes(url);
    }

    private record PageInitializationContext(WebDriver driver, String pageName, iWebElement scopeRoot) {
    }
}
