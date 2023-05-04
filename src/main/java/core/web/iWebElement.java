package core.web;

import core.tools.CacheValue;
import core.web.conditions.HiddenElementCondition;
import org.openqa.selenium.*;
import org.openqa.selenium.By.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.JsWorker;
import utils.logging.iLogger;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class iWebElement implements WebElement {
    private static final int WAIT_TIMEOUT_SEC = 5;
    private static final int SLEEP_TIMEOUT_MS = 100;
    protected final WebDriver driver;
    protected final String name;
    protected final WebDriverWait wait;
    private final CacheValue<WebElement> cachedWebElement = new CacheValue<>();
    protected By byLocator;
    protected String copiedByLocator;
    private boolean shouldBeCached = false;
    private final JsWorker jsWorker;

    public iWebElement(WebDriver driver, String name) {
        this.driver = driver;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC), Duration.ofSeconds(SLEEP_TIMEOUT_MS));
        jsWorker = new JsWorker(driver);
        this.name = name;
    }

    public iWebElement(WebDriver driver, String name, By locator, WebElement el) {
        this(driver, name);
        this.byLocator = locator;
        this.setWebElement(el);
    }

    public By getLocator() {
        return byLocator;
    }

    public void setLocator(By locator) {
        byLocator = locator;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement getWebElement() {
        if (shouldBeCached && cachedWebElement.hasValue()) {
            jsWorker.highlightElement(cachedWebElement.get());
            return cachedWebElement.get();
        } else {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(byLocator));
                jsWorker.highlightElement(element);
                if (shouldBeCached) {
                    cachedWebElement.setForce(element);
                }
                return element;
            } catch (TimeoutException e) {
                throw new TimeoutException("Don't see " + this);
            }
        }
    }

    public void setWebElement(WebElement el) {
        cachedWebElement.setForce(el);
    }

    public void click() {
        click(5);
    }

    public void click(int timeout) {
        click(new WebDriverWait(this.driver, Duration.ofSeconds(timeout), Duration.ofSeconds(SLEEP_TIMEOUT_MS)));
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
            jsWorker.stopHighlight(element);
        }
    }

    @Override
    public void submit() {
        wait.until(ExpectedConditions.visibilityOf(this)).submit();
    }

    public void sendKeys(CharSequence... values) {
        String valueString = Arrays.stream(values).map(Object::toString).collect(Collectors.joining(", "));
        iLogger.debug("Send keys [{}] to {}", valueString, name);
        sendText(values);
    }


    private void sendText(CharSequence... charSequences) {
        WebElement element = getWebElement();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.sendKeys(charSequences);
        } catch (TimeoutException e) {
            throw new TimeoutException("Failed to send keys to " + name + " with locator " + byLocator);
        } finally {
            jsWorker.stopHighlight(element);
        }
    }

    public void clear() {
        iLogger.debug("Clear field {}", name);
        WebElement element = getWebElement();
        try {
            jsWorker.executeScript("arguments[0].value = '';", element);
            wait.until(ExpectedConditions.elementToBeClickable(element)).clear();
        } catch (TimeoutException e) {
            throw new TimeoutException("Failed to clean " + name + " with locator " + byLocator);
        } finally {
            jsWorker.stopHighlight(element);
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
        jsWorker.stopHighlight(el);
        return result;
    }

    public boolean isEnabled() {
        WebElement el = getWebElement();
        boolean result = el.isEnabled();
        iLogger.debug("Element " + name + " is enabled = " + result);
        jsWorker.stopHighlight(el);
        return result;
    }

    public String getText() {
        setFocus();
        WebElement el = getWebElement();
        String text = Optional.ofNullable(el.getText()).filter(s -> !s.isBlank()).orElse(el.getAttribute("value"));
        iLogger.debug("Get text -->{}<-- from {}", text, name);
        jsWorker.stopHighlight(el);
        return text;
    }

    public List<WebElement> findElements(By by) {
        return getWebElement().findElements(by);
    }

    public iWebElement findElement(By by) {
        return new iWebElement(driver, String.format("Child of %s", name), by, getWebElement().findElement(by));
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
            jsWorker.stopHighlight(el);
        }
    }

    public Point getLocation() {
        iLogger.debug("Get location for " + name);
        WebElement el = getWebElement();
        jsWorker.stopHighlight(el);
        return el.getLocation();
    }

    public Dimension getSize() {
        iLogger.debug("Get size for " + name);
        WebElement el = getWebElement();
        jsWorker.stopHighlight(el);
        return el.getSize();
    }

    public Rectangle getRect() {
        iLogger.debug("Get Rect for " + name);
        WebElement el = getWebElement();
        jsWorker.stopHighlight(el);
        return el.getRect();
    }

    public String getCssValue(String s) {
        iLogger.debug("Get CSS value " + s + " for " + name);
        WebElement el = getWebElement();
        jsWorker.stopHighlight(el);
        return el.getCssValue(s);
    }

    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        iLogger.debug("Get screenshot for " + name);
        WebElement el = getWebElement();
        jsWorker.stopHighlight(el);
        return el.getScreenshotAs(outputType);
    }

    public String getAttribute(String attribute) {
        iLogger.debug("Get attribute " + attribute + "  for " + name);
        WebElement el = getWebElement();
        jsWorker.stopHighlight(el);
        return el.getAttribute(attribute);
    }

    public String getHref() {
        return getAttribute("href");
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
        jsWorker.scrollAndSetFocus(getWebElement());
    }

    public boolean hasChild(iWebElement child) {
        boolean hasChild = findElements(child.getLocator()).size() > 0;
        iLogger.debug(String.format("Element %s has %s as child = %s", name, child.getLocator(), hasChild));
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
        jsWorker.stopHighlight(el);
    }

    public void setShouldBeCached(boolean shouldBeCached) {
        this.shouldBeCached = shouldBeCached;
    }
}
