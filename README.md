# custom_webelement

This is an open-source framework for automating UI tests using Java 21 and Selenium. The primary distinction from native Selenium is the built-in capability to log all actions by default, such as finding elements, performing clicks, retrieving text, and more.

You can easily integrate this framework into your project if you are already using plain Selenium and following the Page Factory Pattern. To do this, you need to change the default PageFactory initialization to use "iPageFactory" as shown below:

**Before:**

```java
public AbstractPage() {
    this.driver = DriverFactory.initDriver();
    PageFactory.initElements(this.driver, this); // <==== Default Selenium PageFactory
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
}
```

**After:**

```java
public AbstractPage() {
    this.driver = DriverFactory.initDriver();
    iPageFactory.initElements(this.driver, this); // <==== Custom PageFactory from this framework
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
}
```

In addition, this framework offers several additional features. For example, you can use "iWebElement" instead of the standard Selenium "WebElement" to access the following functions:

1. Templates for locators:

    ```java
    @FindBy(xpath = "//button[text()='%s']")
    public iWebElement button;
    // You can use a single webElement for multiple buttons that only differ by text
    button.template("OK").click();
    button.template("SAVE").click();
    ```

2. Checking if an element has text:

    ```java
    @FindBy(xpath = "//label")
    public iWebElement label;
    // This can be used for assertion purposes, for example
    Assert.assertTrue(label.hasText());
    ```

3. Checking if an element has a scrollbar:

    ```java
    @FindBy(xpath = "//input")
    public iWebElement input;
    // This can be used to verify if an input field gets a scrollbar for long text, for instance
    input.sendText(SOME_LONG_TEXT);
    Assert.assertTrue(label.isScrollPresented());
    ```

4. use cached Webelement to speed up your tests:

    ```java
    @FindBy(css = ".gLFyf[type='search']")
    @CacheElement
    private iWebElement cachedSearchInput;
    ```

## Logging policy (test reports)

The framework now applies a status-aware logging policy for test reports:

1. For successful tests, only `INFO` level lines are shown in report log blocks.
2. For failed or skipped tests, full logs are shown (`INFO`, `DEBUG`, `ERROR`).
3. Screenshot references are preserved in reports.

This policy is applied in:

1. Allure `Execution log` attachment.
2. Custom TestNG artifacts report (`build/reports/tests/testng/custom-artifacts.html`).

## Test-step logging recommendations

Use `INFO` for business-visible test steps and user journey actions:

1. Open page.
2. Click element.
3. Send keys / submit form.
4. Assertion checks.

Use `DEBUG` for technical diagnostics that do not represent user/business flow.

## Custom assertions with built-in logs

Yes, this is a good idea if the wrapper stays thin and readable.

Use `utils.assertions.iAssert` for checks with automatic readable logs, for example:

```java
iAssert.equalsTo(actualText, expectedText, "search input contains entered query");
iAssert.isTrue(resultsAreUnique, "search results list contains unique entries");
```

This keeps test code concise and guarantees assertion intent is always visible in logs.

You can find some example of tests in demo test class: 

    src/test/java/tests/DuckDuckGoPageTest.java
    
Feel free to use and contribute to this open-source framework to enhance your UI testing capabilities.
