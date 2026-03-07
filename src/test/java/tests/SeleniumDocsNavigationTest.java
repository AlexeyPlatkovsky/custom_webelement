package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SeleniumWindowsPage;

@Test(groups = {"ui"})
public class SeleniumDocsNavigationTest extends BaseTest {
    @Test
    public void openPageTest() {
        SeleniumWindowsPage windowsPage = new SeleniumWindowsPage();
        windowsPage.openPage();
        Assert.assertTrue(
                windowsPage.isExpectedPathOpened(),
                "Expected selenium.dev documentation child page was not opened"
        );
    }
}
