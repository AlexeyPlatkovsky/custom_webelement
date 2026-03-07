package pages;

import core.web.annotations.PageURL;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;

@PageURL(value = "https://github.com")
public class GitHubPage extends AbstractPage {

    @FindBy(id = "hero-section-brand-heading")
    private iWebElement heroHeading;

    @FindBy(css = "a[aria-label='Go to GitHub homepage']")
    private iWebElement githubLogoLink;

    public String getHeroHeadingText() {
        return heroHeading.getText();
    }

    public boolean isGithubLogoPresent() {
        return githubLogoLink.isDisplayed();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
