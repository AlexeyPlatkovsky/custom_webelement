package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SevenPage;

@Test(groups = {"ui"})
public class TinkoffTest extends BaseTest {
    @Test
    public void openPageTest() {
        SevenPage seven = new SevenPage();
        seven.openPage();
        Assert.assertTrue(seven.isSevenPresent(), "#seven is not present");
    }
}
