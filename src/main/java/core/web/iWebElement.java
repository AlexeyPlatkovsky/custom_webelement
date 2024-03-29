package core.web;

import core.tools.CacheValue;
import core.web.conditions.HiddenElementCondition;
import core.web.conditions.TextCondition;
import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.By.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import utils.logging.iLogger;
import utils.properties.WebElementProperties;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
public class iWebElement implements WebElement {
    private static final int WAIT_TIMEOUT_SEC = 5;
    protected final WebDriver driver;
    protected final String name;
    protected final WebDriverWait wait;
    private final CacheValue<WebElement> cachedWebElement = new CacheValue<>();
    protected By byLocator;
    protected String copiedByLocator;
    private boolean shouldBeCached = false;
    private final JavascriptExecutor jsExecutor;

    public iWebElement(WebDriver driver, String name) {
        this.driver = driver;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC));
        jsExecutor = (JavascriptExecutor) driver;
        this.name = name;
    }

    public iWebElement(WebDriver driver, String name, By byLocator) {
        this.driver = driver;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC));
        jsExecutor = (JavascriptExecutor) driver;
        this.byLocator = byLocator;
        this.name = name;
    }

    public iWebElement(WebDriver driver, String name, By locator, WebElement el) {
        this.driver = driver;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC));
        jsExecutor = (JavascriptExecutor) driver;
        this.name = name;
        this.byLocator = locator;
        this.setWebElement(el);
    }

    public By getLocator() {
        return byLocator;
    }

    public String getLocatorValue() {
        return byLocator.toString().replaceAll("(By\\.)(\\w+)(: )", "").trim();
    }

    public void setLocator(By locator) {
        byLocator = locator;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement getWebElement(boolean isHighlight) {
        if (shouldBeCached && cachedWebElement.hasValue()) {
            if (isHighlight) {
                highlightElement();
            }
            return cachedWebElement.get();
        } else {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(byLocator));
                if (isHighlight) {
                    highlightElement(element);
                }
                if (shouldBeCached) {
                    cachedWebElement.setForce(element);
                }
                return element;
            } catch (TimeoutException e) {
                throw new SkipException("Don't see " + this);
            }
        }
    }

    public WebElement getWebElement() {
        return getWebElement(true);
    }

    public void setWebElement(WebElement el) {
        cachedWebElement.setForce(el);
        shouldBeCached = true;
    }

    private void highlightElement() {
        highlightElement(cachedWebElement.get());
    }

    private void highlightElement(WebElement element) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        boolean shouldBeHighlighted = Boolean.parseBoolean(WebElementProperties.WEBELEMENT_BORDER_SHOULD_BE_HIGHLIGHTED);
        if (shouldBeHighlighted) {
            String border = String.format("arguments[0].style.border='%s solid %s'",
                    WebElementProperties.WEBELEMENT_BORDER_WIDTH, WebElementProperties.WEBELEMENT_BORDER_COLOR);
            try {
                executeScript(border, element);
            } catch (Exception ignore) {
            }
        }
    }

    private void stopHighlight(WebElement element) {
        try {
            executeScript("arguments[0].style.border=''", element);
        } catch (Exception ignored) {
        }
    }

    private void stopHighlight() {
        try {
            executeScript("arguments[0].style.border=''", getWebElement(false));
        } catch (Exception ignored) {
        }
    }

    public void click() {
        click(5);
    }

    public void click(int timeout) {
        click(new WebDriverWait(this.driver, Duration.ofSeconds(timeout)));
    }

    private void click(WebDriverWait waitForClick) {
        iLogger.debug("Click on {}", toString());
        WebElement element = getWebElement();
        try {
            waitForClick.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            try {
                waitForClick.until(new HiddenElementCondition(element));
            } catch (TimeoutException ex) {
                if (driver.findElements(byLocator).size() == 0) {
                    throw new NoSuchElementException("No such element " + this);
                }
                throw new TimeoutException("Failed to click with JS on " + this);
            }
        } finally {
            stopHighlight(element);
        }
    }

    @Override
    public void submit() {
        wait.until(ExpectedConditions.visibilityOf(this)).submit();
    }

    public void sendKeys(CharSequence... value) {
        iLogger.debug("Send keys " + Arrays.toString(value) + " to " + name);
        sendText(value);
    }

    public void sendKeys(Keys value) {
        iLogger.debug("Send keys " + value.name() + " to " + name);
        Actions actions = new Actions(driver);
        actions.sendKeys(value).perform();
    }

    public void sendText(CharSequence... charSequences) {
        WebElement element = getWebElement();
        element.sendKeys(charSequences);
        stopHighlight(element);
    }

    public void clear() {
        iLogger.debug("Clear field {}", name);
        WebElement element = getWebElement();
        try {
            executeScript("arguments[0].value = '';", element);
            wait.until(ExpectedConditions.elementToBeClickable(element)).clear();
        } catch (TimeoutException e) {
            throw new TimeoutException("Failed to clean " + name + " with locator " + byLocator);
        } finally {
            stopHighlight(element);
        }
    }

    public String getTagName() {
        iLogger.debug("Get tag name for {}", name);
        return getWebElement().getTagName();
    }

    public boolean isSelected() {
        WebElement el = getWebElement();
        boolean result = el.isSelected();
        iLogger.debug("Element " + name + " is selected = " + result);
        stopHighlight(el);
        return result;
    }

    public boolean isEnabled() {
        WebElement el = getWebElement();
        boolean result = el.isEnabled();
        iLogger.debug("Element " + name + " is enabled = " + result);
        stopHighlight(el);
        return result;
    }

    public String getText() {
        setFocus();
        WebElement el = getWebElement();
        String text = el.getText();
        String value = el.getAttribute("value");
        String returnText;
        if (!isBlank(text)) {
            iLogger.debug("Get inner text -->" + text + "<-- from " + name);
            returnText = text;
        } else if (!isBlank(value)) {
            iLogger.debug("Get value text -->" + value + "<-- from " + name);
            returnText = value;
        } else {
            el = getWebElement();
            text = el.getText();
            iLogger.debug("Get inner text -->" + text + "<-- from " + name);
            returnText = text;
        }
        stopHighlight(el);
        return returnText;
    }

    public List<WebElement> findElements(By by) {
        return getWebElement().findElements(by);
    }

    public iWebElement findElement(By by) {
        return new iWebElement(driver, String.format("Child of %s", name), by, getWebElement(false).findElement(by));
    }

    public iWebElement getChild(iWebElement webElement) {
        return findElement(webElement.getLocator());
    }

    public iWebElement getChild(By by) {
        return findElement(by);
    }

    public List<WebElement> getChildren(iWebElement webElement) {
        return findElements(webElement.getLocator());
    }

    public boolean isDisplayed() {
        WebElement el = getWebElement();
        try {
            iLogger.debug("Element " + name + " is displayed = " + el.isDisplayed());
            return el.isDisplayed();
        } catch (Exception ex) {
            iLogger.debug("Element " + name + " is displayed = " + false);
            return false;
        } finally {
            stopHighlight(el);
        }
    }

    public Point getLocation() {
        iLogger.debug("Get location for " + name);
        WebElement el = getWebElement();
        stopHighlight(el);
        return el.getLocation();
    }

    public Dimension getSize() {
        iLogger.debug("Get size for " + name);
        WebElement el = getWebElement();
        stopHighlight(el);
        return el.getSize();
    }

    public Rectangle getRect() {
        iLogger.debug("Get Rect for " + name);
        WebElement el = getWebElement();
        stopHighlight(el);
        return el.getRect();
    }

    public String getCssValue(String s) {
        iLogger.debug("Get CSS value " + s + " for " + name);
        WebElement el = getWebElement();
        stopHighlight(el);
        return el.getCssValue(s);
    }

    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        iLogger.debug("Get screenshot for " + name);
        WebElement el = getWebElement();
        stopHighlight(el);
        return el.getScreenshotAs(outputType);
    }

    public String getAttribute(String attribute) {
        iLogger.debug("Get attribute " + attribute + "  for " + name);
        WebElement el = getWebElement();
        stopHighlight(el);
        return el.getAttribute(attribute);
    }

    public String getHref() {
        return getAttribute("href");
    }

    private Object executeScript(String s) {
        return jsExecutor.executeScript(s);
    }

    private Object executeScript(String s, WebElement element) {
        return jsExecutor.executeScript(s, element);
    }

    private Object executeScript(String s, WebElement element, WebElement targetElement) {
        return jsExecutor.executeScript(s, element, targetElement);
    }

    private Object executeScript(String s, WebElement webElement, int cursorPosition) {
        return jsExecutor.executeScript(s, webElement, cursorPosition);
    }

    public iWebElement template(String text) {
        setupLocator(text);
        return this;
    }

    protected void setupLocator(String text) {
        if (copiedByLocator == null) {
            copiedByLocator = byLocator.toString();
        }
        String locator = copiedByLocator.replace("%s", text).replaceAll("(By\\.)(\\w+)(: )", "").trim();
        switch (byLocator.getClass().getSimpleName()) {
            case "ById" -> byLocator = new ById(locator);
            case "ByLinkText" -> byLocator = new ByLinkText(locator);
            case "ByPartialLinkText" -> byLocator = new ByPartialLinkText(locator);
            case "ByName" -> byLocator = new ByName(locator);
            case "ByTagName" -> byLocator = new ByTagName(locator);
            case "ByXPath" -> byLocator = new ByXPath(locator);
            case "ByClassName" -> byLocator = new ByClassName(locator);
            case "ByCssSelector" -> byLocator = new ByCssSelector(locator);
            default -> {
                iLogger.error("Unknown By class: " + byLocator.getClass().getSimpleName());
                throw new IllegalArgumentException("Unknown By class: " + byLocator.getClass().getSimpleName());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("WebElement %s with locator %s", name, byLocator);
    }

    public void setWaiter(Long waiter) {
        wait.withTimeout(Duration.ofSeconds(waiter));
    }

    public void setFocus() {
        iLogger.debug("Set focus on element {}", toString());
        executeScript("arguments[0].scrollIntoView(true);", this.getWebElement());
        executeScript("arguments[0].focus();", this.getWebElement());
    }

    public boolean hasChild(iWebElement child) {
        boolean hasChild = findElements(child.getLocator()).size() > 0;
        iLogger.debug(String.format("Element %s has %s as child = %s", name, child.getLocator(), hasChild));
        stopHighlight();
        return hasChild;
    }

    protected void waitToBeVisible() {
        wait.until(ExpectedConditions.visibilityOf(this));
    }

    public boolean textIs(String expectedText) {
        iLogger.debug("Check that text {} is presented in {}", expectedText, toString());
        wait.until(TextCondition.textIsNotEmpty(this));
        return getText().equals(expectedText);
    }

    public void hover() {
        Actions actions = new Actions(driver);
        WebElement el = getWebElement();
        actions.moveToElement(el).perform();
        stopHighlight(el);
    }

    public void setShouldBeCached(boolean shouldBeCached) {
        this.shouldBeCached = shouldBeCached;
    }

    public void selectTextInElement() {
        iLogger.debug("Select text in element {}", toString());
        executeScript("let element = document.evaluate(\"" + getLocatorValue() + "\", " +
                "document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;\n" +
                "let range = document.createRange();\n" +
                "range.selectNodeContents(element);\n" +
                "let selection = window.getSelection();\n" +
                "selection.removeAllRanges();\n" +
                "selection.addRange(range);"
        );
        stopHighlight();
    }

    public void actions(Keys key, char keyChar) {
        iLogger.debug("Send keys {} + {} to element {}", key.name(), String.valueOf(keyChar), toString());
        Actions actions = new Actions(driver);
        actions.keyDown(key).keyDown(String.valueOf(keyChar)).keyUp(key).build().perform();
    }

    public boolean isScrollPresented() {
        boolean isScrollPresented = (boolean) executeScript("return arguments[0].scrollHeight > arguments[0].clientHeight;", getWebElement());
        stopHighlight();
        return isScrollPresented;
    }

    public void setCursorAtPosition(int cursorPosition) {
        executeScript(
                "var range = document.createRange();" +
                        "var textNode = arguments[0].firstChild;" +
                        "range.setStart(textNode, arguments[1]);" +
                        "range.setEnd(textNode, arguments[1]);" +
                        "var sel = window.getSelection();" +
                        "sel.removeAllRanges();" +
                        "sel.addRange(range);",
                getWebElement(),
                cursorPosition
        );
        stopHighlight();
    }

    public iWebElement getParent() {
        return this.findElement(By.xpath("./.."));
    }
}
