package pages;

import core.web.annotations.PageURL;
import org.openqa.selenium.WebDriver;

@PageURL(value = "/farewell")
public class Farewell extends TinkoffJournalPage {
    public Farewell(WebDriver driver) {
        super(driver);
    }
}
