package pages;

import core.web.annotations.RelativeURL;
import org.openqa.selenium.WebDriver;

@RelativeURL(relativeUrl = "/#seven")
public class Seven extends Farewell {
    public Seven(WebDriver driver) {
        super(driver);
    }
}
