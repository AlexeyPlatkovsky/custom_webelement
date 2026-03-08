# Claude Code — Project Instructions

## Project Context

This is **custom_webelement**, a Java 21 Selenium 4 test automation framework with an
AI-QA Copilot layer. All production code lives in `src/main/java/`; test code and Page
Objects live in `src/test/java/`.

---

## Java Code Style

- **Java 21** — use modern features: records, sealed classes, pattern matching, switch
  expressions, text blocks, `var` where it improves readability (not where it obscures type)
- **No wildcard imports** — every import must be explicit
- **Lombok** — use `@Value` for immutable data classes, `@Getter`/`@Setter` where needed;
  do not write manual getters/setters for fields that Lombok can generate
- **Checkstyle** is enforced — run `./gradlew checkstyleMain` before committing; never
  suppress Checkstyle warnings without a comment explaining why
- **No `System.out.println`** — use `iLogger` for all logging
- Line length limit: 120 characters

---

## Page Object Pattern

### Structure rules

Every Page Object **must**:

```java
@PageURL("https://example.com/path")   // full URL or relative path
public class LoginPage extends AbstractPage {

    // Fields: iWebElement or iWebElementsList only — never raw WebElement
    @FindBy(css = "#email")
    @CacheElement                       // for stable, static elements
    private iWebElement emailInput;

    @FindBy(css = "#password")
    @CacheElement
    private iWebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private iWebElement loginButton;

    // For repeated elements differing only by text
    @FindBy(xpath = "//nav/a[text()='%s']")
    private iWebElement navLink;        // call navLink.template("Home") at use site

    public LoginPage() {
        iPageFactory.initElements(this.driver, this);
    }

    // Action methods — group related interactions
    public LoginPage enterCredentials(String email, String password) {
        emailInput.sendText(email);
        passwordInput.sendText(password);
        return this;                    // fluent — same page
    }

    public HomePage clickLogin() {
        loginButton.click();
        return new HomePage();          // navigates away — return new page type
    }
}
```

### Mandatory rules

- **Never** use `WebElement` — always `iWebElement` or `iWebElementsList`
- **Never** use `PageFactory` — always `iPageFactory.initElements(this.driver, this)`
- **Never** add `@Test` methods or assertions to Page Objects
- **Never** use `Thread.sleep()` — use `@Waiter(waitFor = N)` or explicit `WebDriverWait`
- Apply `@CacheElement` to all stable, non-dynamic elements (headers, nav, static fields)
- Apply `@Waiter(waitFor = N)` to slow-loading elements instead of arbitrary waits

### Locator priority (best → worst)

1. `id` attribute: `@FindBy(id = "login-btn")`
2. `data-testid`: `@FindBy(css = "[data-testid='login-btn']")`
3. `aria-label`: `@FindBy(css = "[aria-label='Login']")`
4. Stable CSS class: `@FindBy(css = ".login-button")`
5. XPath — only when CSS cannot express the selector

Avoid: positional XPath (`//div[3]/span[2]`), fragile class chains, auto-generated IDs.

### Field naming

- Inputs: `emailInput`, `passwordInput`, `searchField`
- Buttons: `loginButton`, `submitButton`, `cancelButton`
- Links: `forgotPasswordLink`, `homeLink`
- Messages: `errorMessage`, `successBanner`, `loadingSpinner`
- Lists: `searchResults`, `menuItems`, `productCards`

Never use: `element1`, `div3`, `button`, `field`, `el`, `wb`.

---

## Test Code

### Structure

```java
public class LoginTest extends BaseTest {

    @Test(groups = "ui")
    public void userCanLoginWithValidCredentials() {
        LoginPage loginPage = new LoginPage();
        loginPage.openPage();

        HomePage homePage = loginPage
            .enterCredentials("user@example.com", "secret")
            .clickLogin();

        iAssert.assertTrue(homePage.isWelcomeBannerVisible(), "Welcome banner should appear");
    }
}
```

- All UI tests: `@Test(groups = "ui")`
- All unit tests: `@Test(groups = "unit")` in `src/test/java/unit_tests/`
- Disabled/flaky tests: `@Test(groups = "disabled")` — never delete, disable instead
- Test method names: `userCanLoginWithValidCredentials` — plain English, describes the scenario
- One assertion concept per test method where practical

### Assertions

Always use `iAssert` (not TestNG `Assert` directly) — it logs the assertion step:

```java
iAssert.assertTrue(condition, "Descriptive failure message");
iAssert.assertEquals(actual, expected, "Descriptive failure message");
```

### Test data

- No hardcoded production credentials in test code
- Use constants or a dedicated test-data class for repeated values

---

## AI Layer (`src/main/java/ai/`)

- All AI provider classes implement `AiProvider` — never call HTTP directly from tests
- Use `AiProviderFactory.create()` to obtain a provider — never instantiate providers directly
  in test or Page Object code
- `PageCrawlerFacade` is the only entry point for page crawling and Page Object generation
- Generated Page Objects land in `src/test/java/generated/` — always review before committing;
  run `./gradlew compileTestJava -PskipAllure` to catch syntax errors

### Access modifiers for test-accessible methods

Unit tests live in `src/test/java/unit_tests/` (e.g. `unit_tests.ai.provider`) — a
**different** package from the production code (`ai`, `utils`, etc.). Package-private
visibility (`static` without `public`) is therefore NOT accessible from tests.

**Rule:** any method intended to be called from unit tests must be `public`. Do not use
package-private as a "limited public" — it only works when tests are in the exact same
package, which they are not here.

### Test classes that use instance-field mocks

Test classes that store Mockito mocks as instance fields and initialize them in
`@BeforeMethod` must declare `singleThreaded = true` to prevent parallel method execution
from causing `UnfinishedStubbingException`:

```java
@Test(groups = "unit", singleThreaded = true)
public class MyProviderTest {
    private HttpClient mockHttp;
    private HttpResponse<String> mockResponse;
    // ...
}
```

When stubbing methods with generic return types (e.g. `HttpClient.send()`), use
`doReturn` instead of `when().thenReturn()` to avoid Java generics type-inference errors:

```java
// WRONG — won't compile: HttpResponse<String> not assignable to HttpResponse<Object>
when(mockHttp.send(any(), any())).thenReturn(mockResponse);

// CORRECT
doReturn(mockResponse).when(mockHttp).send(any(), any());
```

---

## Logging

Use `iLogger` everywhere — not `System.out`, not Log4j directly:

```java
iLogger.info("Descriptive action message");
iLogger.warn("Something unexpected but recoverable");
iLogger.error("Something that signals a real problem");
```

Log **what** is happening, not **that** something is happening:
- Good: `iLogger.info("Clicking login button for user: " + email)`
- Bad:  `iLogger.info("click")`

---

## Git and Commits

- **All new work must branch from `develop`** — never start from `master` or another
  feature branch
- Branch naming: `feature/*`, `fix/*`, `docs/*`, `claude/*`
- Commit messages: imperative mood, concise subject (`Add LoginPage Page Object`),
  no period at end of subject line
- **Never commit**: API keys, tokens, `.env` files, credentials of any kind
- `ai-provider.properties` is safe to commit (secrets stay in ENV vars)
- Always run before committing, in this order:
  ```sh
  ./gradlew compileTestJava -PskipAllure     # catch access/type errors before running
  ./gradlew checkstyleMain                   # style check
  ./gradlew test -Dsuite=unit -PskipAllure   # unit tests (note: -Dsuite, not -Psuite)
  ```
- `skipAllure` is required in network-restricted environments (e.g. Claude Code sandbox)
  where `plugins-artifacts.gradle.org` is blocked; CI runs without it
- `-Dsuite` sets a JVM system property (what the build reads); `-Psuite` is a Gradle
  project property and does NOT affect suite filtering

---

## Build Commands Reference

```sh
./gradlew test -Dsuite=unit -PskipAllure   # unit tests only (network-restricted env)
./gradlew test -Dsuite=unit                # unit tests only (CI / unrestricted network)
./gradlew test -Dsuite=ui                  # UI tests (requires browser)
./gradlew test                             # all tests
./gradlew checkstyleMain                   # Checkstyle on main sources
./gradlew checkstyleTest                   # Checkstyle on test sources
./gradlew compileTestJava -PskipAllure     # compile check (fastest, no plugin downloads)
./gradlew compileTestJava                  # compile check (full, needs network)
./gradlew installPlaywrightBrowsers        # download Chromium for PageCrawler
./gradlew allureReport                     # generate Allure HTML report
```

---

## What NOT to Do

- Do not add new dependencies without checking whether the JDK or an existing dep covers it
- Do not create utility/helper classes for one-off operations — inline it
- Do not add comments to code that already reads clearly; only comment non-obvious logic
- Do not add `try/catch` blocks that swallow exceptions silently
- Do not write Page Objects that extend anything other than `AbstractPage`
- Do not call `driver.findElement()` directly in test code — that belongs in the Page Object
- Do not use `@BeforeClass` / `@AfterClass` — use `@BeforeMethod` / `@AfterMethod` (TestNG)
