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
Some elementary actions already have built-in `INFO` level logging:
1. Click element (`iWebElement.click()`).
2. Send keys (`iWebElement.sendKeys(...)`).
3. Assertion checks via `utils.assertions.iAssert`.
4. Submit a form (`iWebElement.submit()`).
5. Clear a field (`iWebElement.clear()`).
5. Page navigation when your page object logs it (for example, `openPage()` in the demo `AbstractPage`).

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

## AI Page Object Generator

The framework includes an AI-powered Page Object generator that crawls a live page and produces
a ready-to-use Page Object Java class automatically.

### Prerequisites

- An Anthropic API key with credit balance — set it as the `ANTHROPIC_API_KEY` environment variable
- Playwright browsers installed (one-time setup):

```sh
./gradlew installPlaywrightBrowsers
```

### Generate a Page Object from a URL

```java
import ai.crawler.PageCrawlerFacade;
import ai.provider.AnthropicProvider;
import ai.provider.AuthConfig;

AuthConfig auth = new AuthConfig(AuthConfig.AuthType.API_KEY,
    System.getenv("ANTHROPIC_API_KEY"));
AnthropicProvider provider = new AnthropicProvider(auth, "claude-sonnet-4-6");

// Single URL — crawl + generate + write to src/test/java/generated/
Path written = PageCrawlerFacade.generateAndWrite("https://example.com/login", provider);
```

The generated `.java` file lands in `src/test/java/generated/`. Always run a compile check
after generation to catch any issues before running tests:

```sh
./gradlew compileTestJava -PskipAllure
```

### Generate Page Objects for multiple URLs (batch)

```java
List<Path> files = PageCrawlerFacade.generateAndWriteAll(
    List.of(
        "https://example.com/login",
        "https://example.com/dashboard",
        "https://example.com/checkout"
    ),
    provider
);
```

After the batch completes a structured summary is logged:

```
PageCrawlerFacade: batch complete — 2/3 page objects generated
  OK  LoginPage <- https://example.com/login
  OK  DashboardPage <- https://example.com/dashboard
  ERR https://example.com/checkout [FAILED: timeout after 30000ms]
```

### Run the end-to-end smoke test

```sh
ANTHROPIC_API_KEY=<your-key> ./gradlew test -Dsuite=smoke -PskipAllure
```

