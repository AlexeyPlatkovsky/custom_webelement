package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.GooglePage;

@Test(groups = {"ui"}, singleThreaded = true)
public class GooglePageTest extends BaseTest {
  GooglePage googlePage;

  @BeforeMethod
  public void initTest() {
    googlePage = new GooglePage(driver);
  }

  @Test
  public void checkSearchText() {
    String searchText = "Find webdriver specification";
    googlePage.navigate();
    googlePage.searchForText(searchText);
    Assert.assertEquals(googlePage.getTextFromSearchInput(), searchText);
  }
}
