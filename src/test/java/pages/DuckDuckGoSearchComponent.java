package pages;

import core.web.iPage;
import core.web.iWebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

@FindBy(css = "main")
public class DuckDuckGoSearchComponent extends iPage {
    @FindBy(css = "textarea[name='q'], input[name='q']")
    private iWebElement searchInput;

    public void inputSearchText(String searchText) {
        searchInput.sendKeys(searchText);
    }

    public void executeSearch() {
        searchInput.sendKeys(Keys.ENTER);
    }

    public void searchForText(String searchText) {
        iLogger.info("Search text '{}' via page component", searchText);
        inputSearchText(searchText);
        executeSearch();
    }

    public String getTextFromSearchInput() {
        iLogger.info("Get text from search input via page component");
        return searchInput.getText();
    }
}
