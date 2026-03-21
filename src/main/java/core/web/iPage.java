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

public abstract class iPage {
    private static final String RELATIVE_URL_ANNOTATION_NOT_SPECIFIED = "Page URL is not specified in @RelativeURL annotation for class ";
    private static final ThreadLocal<PageInitializationContext> PAGE_INITIALIZATION_CONTEXT = new ThreadLocal<>();
    protected WebDriver driver;
    protected WebDriverWait wait;
    private String pageName;

    public iPage() {
        PageInitializationContext initContext = PAGE_INITIALIZATION_CONTEXT.get();
        if (initContext != null) {
            initializePage(initContext.driver(), initContext.pageName());
        } else {
            initializePage(DriverFactory.getCurrentDriver(), getClass().getSimpleName());
        }
    }

    protected final void initializePage(WebDriver driver, String pageName) {
        this.driver = driver;
        this.pageName = pageName;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        iPageFactory.initElements(this.driver, this);
    }

    static void beginPageInitialization(WebDriver driver, String pageName) {
        PAGE_INITIALIZATION_CONTEXT.set(new PageInitializationContext(driver, pageName));
    }

    static void clearPageInitialization() {
        PAGE_INITIALIZATION_CONTEXT.remove();
    }

    public void openPage() {
        String absoluteUrl = getAbsoluteURL();
        iLogger.info("Go to page " + pageName + " with absolute URL " + absoluteUrl);
        navigateToUrl(absoluteUrl);
    }

    private String getAbsoluteURL() {
        Class<?> pageClass = getClass();
        PageURL pageURL = pageClass.getAnnotation(PageURL.class);
        if (pageURL == null) {
            throw new RuntimeException(RELATIVE_URL_ANNOTATION_NOT_SPECIFIED + pageClass.getName());
        }

        if (pageURL.value().startsWith("http")) {
            return pageURL.value();
        }

        StringBuilder relativeUrl = new StringBuilder();
        do {
            PageURL annotation = pageClass.getAnnotation(PageURL.class);
            if (annotation != null) {
                relativeUrl.insert(0, StringUtil.formatRelativeURL(annotation.value()));
            } else
                throw new RuntimeException(RELATIVE_URL_ANNOTATION_NOT_SPECIFIED + pageClass.getName()
                        + " or its parent");
            pageClass = pageClass.getSuperclass();
        } while (pageClass != iPage.class);

        return Environment.getRootUrl() + relativeUrl;
    }

    private void navigateToUrl(String url) {
        try {
            URL validatedUrl = new URI(url).toURL();
            driver.get(validatedUrl.toString());
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }

    private record PageInitializationContext(WebDriver driver, String pageName) {
    }
}
