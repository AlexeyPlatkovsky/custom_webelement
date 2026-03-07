package tests;

import org.testng.annotations.Test;
import pages.SeleniumWindowsPage;
import utils.assertions.iAssert;
import utils.logging.iLogger;

@Test(groups = {"ui"})
public class SeleniumDocsNavigationTest extends BaseTest {
    @Test
    public void openPageTest() {
        SeleniumWindowsPage windowsPage = new SeleniumWindowsPage();
        iLogger.info("Step: open Selenium Windows documentation page");
        windowsPage.openPage();
        iAssert.isTrue(
                windowsPage.isExpectedPathOpened(),
                "Selenium Windows documentation path is opened"
        );
    }
}
