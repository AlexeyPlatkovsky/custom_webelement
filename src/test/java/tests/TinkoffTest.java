package tests;

import core.driver.DriverFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.Seven;

@Test(groups = {"ui"})
public class TinkoffTest extends BaseTest {
    @Test
    public void openPageTest() {
        Seven seven = new Seven(DriverFactory.initDriver());
        seven.openPage();
        Assert.assertTrue(seven.isSevenPresent(), "#seven is not present");
    }
}
