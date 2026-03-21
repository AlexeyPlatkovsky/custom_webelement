package tests;

import org.testng.annotations.Test;
import pages.DuckDuckGoPage;
import utils.assertions.iAssert;
import utils.logging.iLogger;

import java.util.List;
import java.util.Set;

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
                "search input does not contain entered query"
        );
    }

    @Test(description = "Compare performance of cached and non-cached elements")
    public void compareCachedAndNonCachedElementsPerformanceTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "web element implementation performance";
        duckDuckGoPage.openPage();
        duckDuckGoPage.inputSearchText(searchText);
        long cached = duckDuckGoPage.getCachedElementFindTime();
        duckDuckGoPage.openPage();
        duckDuckGoPage.inputSearchText(searchText);
        long nonCached = duckDuckGoPage.getNonCachedElementFindTime();
        iAssert.isTrue(cached < nonCached, "cached lookup is slower than non-cached lookup");
    }

    @Test(description = "Check that iWebElementsList works correctly")
    public void checkWorkOfIWebElementsListTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "best java testing frameworks";
        duckDuckGoPage.openPage();
        duckDuckGoPage.searchForText(searchText);
        List<String> elementTexts = duckDuckGoPage.getAllSearchResults();
        iAssert.isTrue(
                Set.copyOf(elementTexts).size() == elementTexts.size(),
                "search results list contains not only unique entries"
        );
    }
}
