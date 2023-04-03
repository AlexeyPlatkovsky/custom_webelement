package pages;

import core.Environment;
import core.web.annotations.RelativeURL;
import core.web.iPageFactory;
import org.openqa.selenium.WebDriver;
import utils.StringUtil;
import utils.logging.iLogger;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractPage {
    public WebDriver driver;

    public AbstractPage(WebDriver driver) {
        this.driver = driver;
        iPageFactory.initElements(this.driver, this);
    }

    public void openPage() {
        String absoluteUrl = getAbsoluteURL();
        if (!absoluteUrl.isEmpty()) {
            navigateToUrl(absoluteUrl);
            iLogger.info("Page " + getClass().getSimpleName() + " with absolute URL" + absoluteUrl + " is opened");
        } else {
            throw new RuntimeException("Page URL is not specified in @RelativeURL annotation");
        }
    }

    private String getAbsoluteURL() {
        StringBuilder relativeUrl = new StringBuilder();
        Class<?> pageClass = getClass();
        while (pageClass != AbstractPage.class) {
            RelativeURL annotation = pageClass.getAnnotation(RelativeURL.class);
            if (annotation != null) {
                relativeUrl.insert(0, StringUtil.formatRelativeURL(annotation.relativeUrl()));
            } else
                throw new RuntimeException("Page URL is not specified in @RelativeURL annotation for class "
                        + pageClass.getName() + " or its parent");

            pageClass = pageClass.getSuperclass();
        }
        return Environment.getBaseUrl() + relativeUrl;
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
