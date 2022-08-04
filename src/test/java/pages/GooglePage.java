package pages;

import core.web.iWebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class GooglePage extends AbstractPage {
  private static final String URL = "https://www.google.com/";

  @FindBy(css = "[aria-label='Search']")
  private iWebElement searchInput;

  public GooglePage(WebDriver driver) {
    super(driver);
  }

  public void navigate() {
    driver.navigate().to(URL);
  }

  public void inputSearchText(String searchText) {
    searchInput.setText(searchText);
  }

  public void executeSearch() {
    searchInput.sendKeys(Keys.ENTER);
  }

  public void searchForText(String searchText) {
    inputSearchText(searchText);
    executeSearch();
  }

  public String getTextFromSearchInput() {
    return searchInput.getText();
  }
}
