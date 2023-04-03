package pages;

import core.web.annotations.RelativeURL;
import org.openqa.selenium.WebDriver;

@RelativeURL(relativeUrl = "/farewell")
public class Farewell extends TinkoffJournalPage {
    public Farewell(WebDriver driver) {
        super(driver);
    }
}
