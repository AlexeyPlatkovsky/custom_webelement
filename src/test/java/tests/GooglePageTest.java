package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.GooglePage;

@Test(groups = {"ui"})
public class GooglePageTest extends BaseTest {
    @Test
    public void searchGoogleSpecificationTest() {
        GooglePage googlePage = new GooglePage();
        String searchText = "Find chrome specification";
        googlePage.openPage();
        googlePage.searchForText(searchText);
        Assert.assertEquals(googlePage.getTextFromSearchInput(), searchText);
    }

//    @Test(description = "Check that screenshot is attached to test report if test is failed." +
//            "This test should fail")
//    public void failSearchFireFoxSpecificationTest() {
//        GooglePage googlePage = new GooglePage();
//        String searchText = "Find firefox specification";
//        googlePage.openPage();
//        googlePage.searchForText(searchText);
//        Assert.assertEquals(googlePage.getTextFromSearchInput(), "searchText");
//    }

    @Test(singleThreaded = true,
            description = "Compare performance of cached and non-cached elements")
    public void compareCachedAndNonCachedElementsPerformanceTest() {
        GooglePage googlePage = new GooglePage();
        String searchText = "Compare performance of webElement implementations";
        googlePage.openPage();
        googlePage.searchForText(searchText);
        long cached = googlePage.getCachedElementFindTime();
        googlePage.openPage();
        googlePage.searchForText(searchText);
        long nonCached = googlePage.getNonCachedElementFindTime();
        Assert.assertTrue(cached < nonCached);
    }

    @Test(description = "Check that iWebElementsList works correctly")
    public void checkWorkOfIWebElementsListTest() {
        GooglePage googlePage = new GooglePage();
        String searchText = "chromebook lenovo";
        googlePage.openPage();
        googlePage.searchForText(searchText);
        Assert.assertTrue(googlePage.checkThatAllSearchResultsAreUnique());
    }
}
