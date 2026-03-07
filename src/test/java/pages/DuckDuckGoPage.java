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

@PageURL(value = "https://duckduckgo.com/")
public class DuckDuckGoPage extends AbstractPage {
    @FindBy(css = "textarea[name='q'], input[name='q']")
    private iWebElement searchInput;

    @FindBy(css = "textarea[name='q'], input[name='q']")
    @CacheElement
    private iWebElement cachedSearchInput;

    @FindBy(xpath = "//article//h2//a")
    private iWebElementsList searchResults;

    public void inputSearchText(String searchText) {
        searchInput.sendKeys(searchText);
    }

    public void executeSearch() {
        searchInput.sendKeys(Keys.ENTER);
    }

    public void searchForText(String searchText) {
        iLogger.info("Search text '" + searchText + "'");
        inputSearchText(searchText);
        executeSearch();
    }

    public String getTextFromSearchInput() {
        iLogger.info("Get text from search input");
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
        for (int i = 0; i < 500; i++) {
            el.getText();
        }
        long executionTime = System.currentTimeMillis() - withoutStartTime;
        iLogger.info("Find time for element " + el + " = " + executionTime);
        return executionTime;
    }

    public boolean checkThatAllSearchResultsAreUnique() {
        List<String> elementTexts = searchResults.getTextForVisibleElements();
        Set<String> uniqueTexts = new HashSet<>(elementTexts);
        iLogger.info("Elements for search results are: " + elementTexts);
        return elementTexts.size() == uniqueTexts.size();
    }
}
