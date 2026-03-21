package tests;

import org.testng.annotations.Test;
import pages.ComposedDuckDuckGoPage;
import utils.assertions.iAssert;
import utils.logging.iLogger;

@Test(groups = {"ui"})
public class ComposedDuckDuckGoPageTest extends BaseTest {
    @Test
    public void searchSpecificationThroughPageComponentTest() {
        ComposedDuckDuckGoPage duckDuckGoPage = new ComposedDuckDuckGoPage();
        String searchText = "selenium webdriver documentation";
        duckDuckGoPage.openPage();
        duckDuckGoPage.searchForText(searchText);
        iAssert.equalsTo(
                duckDuckGoPage.getTextFromSearchInput(),
                searchText,
                "search input does not contain entered query"
        );
        iAssert.isTrue(
                duckDuckGoPage.hasSearchResults(),
                "search results are not displayed for composed page"
        );
    }
}
