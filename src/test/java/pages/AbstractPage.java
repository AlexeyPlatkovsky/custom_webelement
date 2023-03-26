package pages;

import core.web.iPageFactory;
import org.openqa.selenium.WebDriver;

public abstract class AbstractPage {
    public WebDriver driver;

    public AbstractPage(WebDriver driver) {
        this.driver = driver;
        iPageFactory.initElements(this.driver, this);
    }
}
