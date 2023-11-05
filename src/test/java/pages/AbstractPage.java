package pages;

import core.Environment;
import core.driver.DriverFactory;
import core.web.annotations.PageURL;
import core.web.iPageFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.StringUtil;
import utils.logging.iLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public abstract class AbstractPage {
    private static final String RELATIVE_URL_ANNOTATION_NOT_SPECIFIED = "Page URL is not specified in @RelativeURL annotation for class ";
    protected WebDriver driver;
    protected WebDriverWait wait;

    public AbstractPage() {
        this.driver = DriverFactory.getCurrentDriver();
        iPageFactory.initElements(this.driver, this);
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
    }

    public void openPage() {
        String absoluteUrl = getAbsoluteURL();
        navigateToUrl(getAbsoluteURL());
        iLogger.info("Page " + getClass().getSimpleName() + " with absolute URL " + absoluteUrl + " is opened");
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
        } while (pageClass != AbstractPage.class);

        return Environment.getRootUrl() + relativeUrl;
    }

    private void navigateToUrl(String url) {
        try {
            URL validatedUrl = new URL(url);
            driver.get(validatedUrl.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }
}
