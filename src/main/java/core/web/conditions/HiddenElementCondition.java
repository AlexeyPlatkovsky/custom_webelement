package core.web.conditions;

import core.driver.DriverFactory;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import utils.JsWorker;
import utils.logging.iLogger;

public class HiddenElementCondition implements ExpectedCondition {

    WebElement element;

    public HiddenElementCondition(WebElement element) {
        iLogger.info("Try to click element with JS");
        this.element = element;
    }

    @Override
    public Boolean apply(Object input) {
        try {
            JsWorker jsWorker = new JsWorker(DriverFactory.getCurrentDriver());
            jsWorker.scrollAndSetFocus(element);
            try {
                element.click();
            } catch (WebDriverException ex) {
                element.sendKeys(Keys.RETURN);
            }
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }
}