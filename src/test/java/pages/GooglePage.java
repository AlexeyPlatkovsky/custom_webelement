package pages;

import core.web.annotations.CacheElement;
import core.web.annotations.RelativeURL;
import core.web.iWebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

@RelativeURL(relativeUrl = "https://www.google.com/")
public class GooglePage extends AbstractPage {
    @FindBy(css = "input.gLFyf")
    private iWebElement searchInput;

    @FindBy(css = "input.gLFyf")
    @CacheElement
    private iWebElement cachedSearchInput;

    public GooglePage(WebDriver driver) {
        super(driver);
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

    public long getCachedElementFindTime() {
        return getElementFindTime(cachedSearchInput);
    }

    public long getNonCachedElementFindTime() {
        return getElementFindTime(searchInput);
    }

    private long getElementFindTime(iWebElement el) {
        long withoutStartTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            el.getText();
        }
        long executionTime = System.currentTimeMillis() - withoutStartTime;
        iLogger.info("Find time for element " + el + " = " + executionTime);
        return executionTime;
    }
}
