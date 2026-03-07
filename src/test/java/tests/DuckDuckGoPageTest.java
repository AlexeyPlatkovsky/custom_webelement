package tests;

import org.testng.annotations.Test;
import pages.DuckDuckGoPage;
import utils.assertions.iAssert;
import utils.logging.iLogger;

@Test(groups = {"ui"})
public class DuckDuckGoPageTest extends BaseTest {
    @Test
    public void searchSpecificationTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "selenium webdriver documentation";
        duckDuckGoPage.openPage();
        duckDuckGoPage.searchForText(searchText);
        iAssert.equalsTo(
                duckDuckGoPage.getTextFromSearchInput(),
                searchText,
                "search input contains entered query"
        );
    }

    @Test(description = "Compare performance of cached and non-cached elements")
    public void compareCachedAndNonCachedElementsPerformanceTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "web element implementation performance";
        iLogger.info("Step: open search page");
        duckDuckGoPage.openPage();
        iLogger.info("Step: search with cached element");
        duckDuckGoPage.inputSearchText(searchText);
        long cached = duckDuckGoPage.getCachedElementFindTime();
        iLogger.info("Step: rerun search with non-cached element");
        duckDuckGoPage.openPage();
        duckDuckGoPage.inputSearchText(searchText);
        long nonCached = duckDuckGoPage.getNonCachedElementFindTime();
        iAssert.isTrue(cached < nonCached, "cached lookup is faster than non-cached lookup");
    }

    @Test(description = "Check that iWebElementsList works correctly")
    public void checkWorkOfIWebElementsListTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "best java testing frameworks";
        iLogger.info("Step: open search page");
        duckDuckGoPage.openPage();
        iLogger.info("Step: search for text '" + searchText + "'");
        duckDuckGoPage.searchForText(searchText);
        iAssert.isTrue(
                duckDuckGoPage.checkThatAllSearchResultsAreUnique(),
                "search results list contains unique entries"
        );
    }
}
