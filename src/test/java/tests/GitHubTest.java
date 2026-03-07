package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.GitHubPage;

@Test(groups = {"ui"})
public class GitHubTest extends BaseTest {

    @Test(description = "Verify GitHub homepage title contains 'GitHub'")
    public void checkGithubPageTitleTest() {
        GitHubPage gitHubPage = new GitHubPage();
        gitHubPage.openPage();
        Assert.assertTrue(gitHubPage.getPageTitle().contains("GitHub"),
                "Page title should contain 'GitHub' but was: " + gitHubPage.getPageTitle());
    }

    @Test(description = "Verify GitHub homepage hero heading is present and not empty")
    public void checkGithubHeroHeadingTest() {
        GitHubPage gitHubPage = new GitHubPage();
        gitHubPage.openPage();
        String heading = gitHubPage.getHeroHeadingText();
        Assert.assertFalse(heading.isEmpty(), "Hero heading text should not be empty");
    }

    @Test(description = "Verify GitHub logo link is present on homepage")
    public void checkGithubLogoLinkTest() {
        GitHubPage gitHubPage = new GitHubPage();
        gitHubPage.openPage();
        Assert.assertTrue(gitHubPage.isGithubLogoPresent(), "GitHub logo link should be present on homepage");
    }
}
