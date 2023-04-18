package pages;

import core.web.annotations.PageURL;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;

@PageURL(value = "/#seven")
public class SevenPage extends FarewellPage {
    @FindBy(css = "[id=seven]")
    private iWebElement seven;

    public boolean isSevenPresent() {
        return seven.isDisplayed();
    }
}
