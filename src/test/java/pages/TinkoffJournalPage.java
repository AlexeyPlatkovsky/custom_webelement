package pages;

import core.web.annotations.RelativeURL;
import org.openqa.selenium.WebDriver;

@RelativeURL()
public class TinkoffJournalPage extends AbstractPage {
    public TinkoffJournalPage(WebDriver driver) {
        super(driver);
    }
}
