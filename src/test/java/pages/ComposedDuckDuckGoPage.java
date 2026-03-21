package pages;

import core.web.annotations.PageURL;
import core.web.iPage;
import core.web.iWebElementsList;
import org.openqa.selenium.support.FindBy;

@PageURL(value = "https://duckduckgo.com/")
public class ComposedDuckDuckGoPage extends iPage {
    private DuckDuckGoSearchComponent searchComponent;

    @FindBy(xpath = "//article//h2//a")
    private iWebElementsList searchResults;

    public void searchForText(String searchText) {
        searchComponent.searchForText(searchText);
    }

    public String getTextFromSearchInput() {
        return searchComponent.getTextFromSearchInput();
    }

    public boolean hasSearchResults() {
        return !searchResults.isEmpty();
    }
}
