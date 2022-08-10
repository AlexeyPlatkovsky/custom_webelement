package tests;

import core.driver.DriverFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.GooglePage;

@Test(groups = {"ui"})
public class GooglePageTest extends BaseTest {

  @Test
  public void searchGoogleSpecification() {
    GooglePage googlePage = new GooglePage(DriverFactory.initDriver());
    String searchText = "Find chrome specification";
    googlePage.navigate();
    googlePage.searchForText(searchText);
    Assert.assertEquals(googlePage.getTextFromSearchInput(), searchText);
  }

  @Test
  public void searchFireFoxSpecification() {
    GooglePage googlePage = new GooglePage(DriverFactory.initDriver());
    String searchText = "Find firefox specification";
    googlePage.navigate();
    googlePage.searchForText(searchText);
    Assert.assertEquals(googlePage.getTextFromSearchInput(), "searchText");
  }
}
