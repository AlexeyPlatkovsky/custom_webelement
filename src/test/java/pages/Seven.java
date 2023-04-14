package pages;

import core.web.annotations.PageURL;
import core.web.iWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@PageURL(value = "/#seven")
public class Seven extends Farewell {
    @FindBy(css = "[id=seven]")
    private iWebElement seven;

    public Seven(WebDriver driver) {
        super(driver);
    }

    public boolean isSevenPresent() {
        return seven.isDisplayed();
    }
}
