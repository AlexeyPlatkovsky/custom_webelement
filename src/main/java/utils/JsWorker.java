package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.properties.WebElementProperties;

public class JsWorker {
    private final JavascriptExecutor jsExecutor;

    public JsWorker(WebDriver driver) {
        jsExecutor = (JavascriptExecutor) driver;
    }

    public void stopHighlight(WebElement element) {
        try {
            jsExecutor.executeScript("arguments[0].style.border=''", element);
        } catch (Exception ignored) {
        }
    }

    public void highlightElement(WebElement element) {
        boolean shouldBeHighlighted = Boolean.parseBoolean(WebElementProperties.WEBELEMENT_BORDER_SHOULD_BE_HIGHLIGHTED);
        if (shouldBeHighlighted) {
            String border = String.format("arguments[0].style.border='%s solid %s'",
                    WebElementProperties.WEBELEMENT_BORDER_WIDTH, WebElementProperties.WEBELEMENT_BORDER_COLOR);
            executeScript(border, element);
        }
    }

    public void scrollAndSetFocus(WebElement element) {
        executeScript("arguments[0].scrollIntoView(true);", element);
        executeScript("arguments[0].focus();", element);
    }

    public void executeScript(String s, WebElement element) {
        jsExecutor.executeScript(s, element);
    }
}
