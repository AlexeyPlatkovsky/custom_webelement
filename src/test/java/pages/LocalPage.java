package pages;

import core.web.annotations.PageURL;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;

@PageURL(value = "/")
public class LocalPage extends AbstractPage {

    @FindBy(id = "main-heading")
    private iWebElement mainHeading;

    @FindBy(css = "p.description")
    private iWebElement description;

    @FindBy(css = "a#test-link")
    private iWebElement testLink;

    public String getMainHeadingText() {
        return mainHeading.getText();
    }

    public String getDescriptionText() {
        return description.getText();
    }

    public boolean isTestLinkVisible() {
        return testLink.isDisplayed();
    }

    public String getTestLinkHref() {
        return testLink.getAttribute("href");
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
