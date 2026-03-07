package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.DuckDuckGoPage;

@Test(groups = {"ui"})
public class DuckDuckGoPageTest extends BaseTest {
    @Test
    public void searchSpecificationTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "selenium webdriver documentation";
        duckDuckGoPage.openPage();
        duckDuckGoPage.searchForText(searchText);
        Assert.assertEquals(duckDuckGoPage.getTextFromSearchInput(), searchText);
    }

//    @Test(description = "Check that screenshot is attached to test report if test is failed." +
//            "This test should fail")
//    public void failSearchFireFoxSpecificationTest() {
//        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
//        String searchText = "Find firefox specification";
//        duckDuckGoPage.openPage();
//        duckDuckGoPage.searchForText(searchText);
//        Assert.assertEquals(duckDuckGoPage.getTextFromSearchInput(), "searchText");
//    }

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
        Assert.assertTrue(cached < nonCached);
    }

    @Test(description = "Check that iWebElementsList works correctly")
    public void checkWorkOfIWebElementsListTest() {
        DuckDuckGoPage duckDuckGoPage = new DuckDuckGoPage();
        String searchText = "best java testing frameworks";
        duckDuckGoPage.openPage();
        duckDuckGoPage.searchForText(searchText);
        Assert.assertTrue(duckDuckGoPage.checkThatAllSearchResultsAreUnique());
    }
}
