package core.web;

import core.tools.CacheValue;
import core.web.conditions.HiddenElementCondition;
import org.openqa.selenium.*;
import org.openqa.selenium.By.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.logging.iLogger;
import utils.properties.WebElementProperties;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.apache.commons.lang3.StringUtils.isBlank;

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

    public iWebElement(WebDriver driver, String name) {
        this.driver = driver;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC), Duration.ofSeconds(SLEEP_TIMEOUT_MS));
        this.name = name;
    }

    public iWebElement(WebDriver driver, String name, By locator, WebElement el) {
        this.driver = driver;
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC), Duration.ofSeconds(SLEEP_TIMEOUT_MS));
        this.name = name;
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
            highlightElement();
            return cachedWebElement.get();
        } else {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(byLocator));
                highlightElement(element);
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

    private void highlightElement() {
        highlightElement(cachedWebElement.get());
    }

    private void highlightElement(WebElement element) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        if (Boolean.parseBoolean(WebElementProperties.WEBELEMENT_BORDER_SHOULD_BE_HIGHLIGHTED)) {
            String border = String.format("arguments[0].style.border='%s solid %s'",
                    WebElementProperties.WEBELEMENT_BORDER_WIDTH, WebElementProperties.WEBELEMENT_BORDER_COLOR);
            jsExecutor.executeScript(border, element);
        }
        if (Boolean.parseBoolean(WebElementProperties.WEBELEMENT_BACKGROUND_SHOULD_BE_HIGHLIGHTED)) {
            String background = String.format("arguments[0].style.background='%s'",
                    WebElementProperties.WEBELEMENT_BACKGROUND_COLOR);
            jsExecutor.executeScript(background, element);
        }
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
        sendText(value);
    }

    private void sendText(CharSequence... charSequences) {
        try {
            WebElement element = getWebElement();
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.sendKeys(charSequences);
        } catch (TimeoutException e) {
            throw new TimeoutException("Failed to send keys to " + name + " with locator " + byLocator);
        }
    }

    public void clear() {
        iLogger.debug("Clear field {}", name);
        try {
            WebElement element = getWebElement();
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].value = '';", element);
            wait.until(ExpectedConditions.elementToBeClickable(element)).clear();
        } catch (TimeoutException e) {
            throw new TimeoutException("Failed to clean " + name + " with locator " + byLocator);
        }
    }

    public String getTagName() {
        iLogger.debug("Get tag name for {}", name);
        return getWebElement().getTagName();
    }

    public boolean isSelected() {
        boolean result = getWebElement().isSelected();
        iLogger.debug("Element " + name + " is selected = " + result);
        return result;
    }

    public boolean isEnabled() {
        boolean result = getWebElement().isEnabled();
        iLogger.debug("Element " + name + " is enabled = " + result);
        return result;
    }

    public String getText() {
        setFocus();
        WebElement el = getWebElement();
        String text = el.getText();
        String value = el.getAttribute("value");
        if (!isBlank(text)) {
            iLogger.debug("Get inner text -->" + text + "<-- from " + name);
            return text;
        }

        if (!isBlank(value)) {
            iLogger.debug("Get value text -->" + value + "<-- from " + name);
            return value;
        } else {
            el = getWebElement();
            text = el.getText();
            iLogger.debug("Get inner text -->" + text + "<-- from " + name);
            return text;
        }
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
        try {
            WebElement el = getWebElement();
            iLogger.debug("Element " + name + " is displayed = " + el.isDisplayed());
            return el.isDisplayed();
        } catch (Exception ex) {
            iLogger.debug("Element " + name + " is displayed = " + false);
            return false;
        }
    }

    public Point getLocation() {
        iLogger.debug("Get location for " + name);
        return getWebElement().getLocation();
    }

    public Dimension getSize() {
        iLogger.debug("Get size for " + name);
        return getWebElement().getSize();
    }

    public Rectangle getRect() {
        iLogger.debug("Get Rect for " + name);
        return getWebElement().getRect();
    }

    public String getCssValue(String s) {
        iLogger.debug("Get CSS value " + s + " for " + name);
        return getWebElement().getCssValue(s);
    }

    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        iLogger.debug("Get screenshot for " + name);
        return getWebElement().getScreenshotAs(outputType);
    }

    public String getAttribute(String attribute) {
        iLogger.debug("Get attribute " + attribute + "  for " + name);
        return getWebElement().getAttribute(attribute);
    }

    public String getHref() {
        return getAttribute("href");
    }

    private void executeScript(String s, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(s, element);
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
        actions.moveToElement(getWebElement()).perform();
    }

    public void setShouldBeCached(boolean shouldBeCached) {
        this.shouldBeCached = shouldBeCached;
    }
}
