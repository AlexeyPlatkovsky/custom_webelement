package core.web.conditions;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import utils.logging.iLogger;

public class HiddenElementCondition implements ExpectedCondition<Boolean> {

    private final WebElement element;

    public HiddenElementCondition(WebElement element) {
        iLogger.info("Try to click element with JS");
        this.element = element;
    }

    @Override
    public Boolean apply(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            js.executeScript("arguments[0].focus();", element);
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
