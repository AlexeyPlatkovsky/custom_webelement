package core.web;

import core.tools.CacheValue;
import org.openqa.selenium.*;
import org.openqa.selenium.By.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.logging.iLogger;

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
  private final CacheValue<WebElement> webElement = new CacheValue<>();
  protected By byLocator;
  protected String copiedByLocator;

  public iWebElement(WebDriver driver, String name) {
    this.driver = driver;
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC), Duration.ofSeconds(SLEEP_TIMEOUT_MS));
    this.name = name;
  }

  public iWebElement(WebDriver driver, String name, String locator) {
    this.driver = driver;
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(WAIT_TIMEOUT_SEC), Duration.ofSeconds(SLEEP_TIMEOUT_MS));
    this.name = name;
    this.byLocator = By.xpath(locator);
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
    if (webElement.hasValue()) {
      return webElement.get();
    } else {
      try {
        return wait.until(ExpectedConditions.presenceOfElementLocated(byLocator));
      } catch (TimeoutException e) {
        throw new TimeoutException("Don't see " + toString());
      }
    }
  }

  public void setWebElement(WebElement el) {
    webElement.setForce(el);
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
        iLogger.takeScreenshot("Can't click element with regular click");
        waitForClick.until(new HiddenCondition(element));
      } catch (TimeoutException ex) {
        if (driver.findElements(byLocator).size() == 0) {
          throw new NoSuchElementException("No such element");
        }
        throw new TimeoutException("Failed to click with JS on " + toString());
      }
    }
  }

  @Override
  public void submit() {
    wait.until(ExpectedConditions.visibilityOf(this)).submit();
  }

  public void sendKeys(CharSequence... value) {
    iLogger.debug("Send keys " + Arrays.toString(value) + " to " + name);
    try {
      WebElement element = getWebElement();
      wait.until(ExpectedConditions.elementToBeClickable(element));
      element.sendKeys(value);
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

  public void setText(CharSequence... value) {
    sendKeys(value);
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
      return el.isDisplayed();
    } catch (Exception ex) {
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

  private void executeScript(String s, WebElement element) {
    ((JavascriptExecutor) driver).executeScript(s, element);
  }

  public iWebElement template(String text) {
    setupLocator(text);
    webElement.setForce(getWebElement());
    return this;
  }

  protected void setupLocator(String text) {
    if (copiedByLocator == null) {
      copiedByLocator = byLocator.toString();
    }
    String locator = String.format(copiedByLocator, text).replaceAll("(By\\.)(\\w+)(: )", "").trim();
    if (ById.class.equals(byLocator.getClass())) {
      byLocator = new ById(locator);
    } else if (ByLinkText.class.equals(byLocator.getClass())) {
      byLocator = new ByLinkText(locator);
    } else if (ByPartialLinkText.class.equals(byLocator.getClass())) {
      byLocator = new ByPartialLinkText(locator);
    } else if (ByName.class.equals(byLocator.getClass())) {
      byLocator = new ByName(locator);
    } else if (ByTagName.class.equals(byLocator.getClass())) {
      byLocator = new ByTagName(locator);
    } else if (ByXPath.class.equals(byLocator.getClass())) {
      byLocator = new ByXPath(locator);
    } else if (ByClassName.class.equals(byLocator.getClass())) {
      byLocator = new ByClassName(locator);
    } else if (ByCssSelector.class.equals(byLocator.getClass())) {
      byLocator = new ByCssSelector(locator);
    }
  }

  @Override
  public String toString() {
    return String.format("Webelement %s with locator %s", name, byLocator);
  }

  public void setWaiter(Long waiter) {
    wait.withTimeout(Duration.ofSeconds(waiter));
  }

  public String getHref() {
    return getAttribute("href");
  }

  public void setFocus() {
    iLogger.info("Set focus on element {}", toString());
    executeScript("arguments[0].scrollIntoView(true);", this.getWebElement());
    executeScript("arguments[0].focus();", this.getWebElement());
  }

  public boolean hasChild(iWebElement child) {
    boolean hasChild = findElements(child.getLocator()).size() > 0;
    iLogger.info(String.format("Element %s has %s as child = %s", name, child.getLocator(), hasChild));
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

  public class HiddenCondition implements ExpectedCondition {

    WebElement element;

    public HiddenCondition(WebElement element) {
      iLogger.info("Try to click element with JS");
      this.element = element;
    }

    @Override
    public Boolean apply(Object input) {
      try {
        executeScript("arguments[0].scrollIntoView(true);", element);
        executeScript("arguments[0].focus();", element);
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
}
