package pages;

import core.web.annotations.PageURL;
import org.openqa.selenium.WebDriver;

@PageURL(value = "/#seven")
public class Seven extends Farewell {
    public Seven(WebDriver driver) {
        super(driver);
    }
}
