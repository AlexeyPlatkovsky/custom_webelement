package pages;

import core.web.annotations.CacheElement;
import core.web.annotations.PageURL;
import core.web.iWebElement;
import core.web.iWebElementsList;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@PageURL(value = "https://www.google.com/")
public class GooglePage extends AbstractPage {
    @FindBy(css = ".gLFyf[type='search']")
    private iWebElement searchInput;

    @FindBy(css = ".gLFyf[type='search']")
    @CacheElement
    private iWebElement cachedSearchInput;

    @FindBy(xpath = "//h3[@class='LC20lb MBeuO DKV0Md']")
    private iWebElementsList searchResults;

    public void inputSearchText(String searchText) {
        searchInput.sendKeys(searchText);
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

    public boolean checkThatAllSearchResultsAreUnique() {
        List<String> elementTexts = searchResults.getTextForVisibleElements();
        Set<String> uniqueTexts = new HashSet<>(elementTexts);
        return elementTexts.size() == uniqueTexts.size();
    }
}
