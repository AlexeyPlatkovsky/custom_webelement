# Getting Started

Use this guide to integrate the framework into an existing Selenium project or set one up from scratch.

## Prerequisites

- Java 21
- Gradle
- Selenium 4 already on the classpath

## Switching from Plain Selenium

Replace the default `PageFactory` initialization with `iPageFactory` in your base page class.

**Before:**

```java
public AbstractPage() {
    this.driver = DriverFactory.initDriver();
    PageFactory.initElements(this.driver, this);
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
}
```

**After:**

```java
public AbstractPage() {
    this.driver = DriverFactory.initDriver();
    iPageFactory.initElements(this.driver, this);
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
}
```

`iPageFactory` is a drop-in replacement. It respects the same `@FindBy` annotations and adds:
- Automatic `iWebElement` / `iWebElementsList` initialization
- Recursive initialization of nested `iPage` components
- `@CacheElement` and `@Waiter` annotation support

## Driver Setup

`DriverFactory.initDriver()` reads the `driver` system property and creates the matching browser session.

| Property | Values | Default behavior |
|---|---|---|
| `driver` | `chrome`, `firefox`, `lambda` | Required |
| `browser_version` | e.g. `114` | Latest if omitted |
| `screen_maximize` | `true` / `false` | Browser default |
| `base_url` | Root URL for fully relative `@PageURL` hierarchies | Required when the page chain has no absolute `@PageURL` |

Pass properties at runtime:

```bash
./gradlew test -Ddriver=chrome -Dbase_url=https://example.com
```

If a page hierarchy includes an absolute `@PageURL` on a base page, that hierarchy does not depend on `base_url`.

## Page Object Skeleton

```java
@PageURL("https://example.com/login")
public class LoginPage extends iPage {

    @FindBy(css = "input[name='username']")
    private iWebElement usernameInput;

    @FindBy(css = "input[name='password']")
    private iWebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private iWebElement submitButton;

    public void login(String user, String pass) {
        usernameInput.sendKeys(user);
        passwordInput.sendKeys(pass);
        submitButton.click();
    }
}
```

Extend `iPage` instead of a plain class. Call `openPage()` (inherited) to navigate using `@PageURL`.

For a shared external domain, prefer an absolute base page plus relative children:

```java
@PageURL("https://practicetestautomation.com")
public abstract class PracticeTestAutomationBasePage extends iPage { }

@PageURL("/practice/")
public class PracticePage extends PracticeTestAutomationBasePage { }
```

For local fixtures or one app environment, keep page URLs relative and provide `-Dbase_url=...` at runtime.

## Running Local Verification

```bash
./gradlew compileJava
./gradlew compileTestJava
./gradlew checkstyleMain
./gradlew checkstyleTest
./gradlew test -Dsuite=unit
```

## Next Steps

- [Writing page objects and components](writing-pages.md)
- [iWebElement API](webelement-api.md)
- [Assertions](assertions.md)
- [Logging and test reports](logging.md)
- [Configuration reference](configuration.md)
