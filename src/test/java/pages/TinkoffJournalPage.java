package pages;

import core.web.annotations.PageURL;
import org.openqa.selenium.WebDriver;

@PageURL(value = "https://journal.tinkoff.ru/")
public class TinkoffJournalPage extends AbstractPage {
    public TinkoffJournalPage(WebDriver driver) {
        super(driver);
    }
}
