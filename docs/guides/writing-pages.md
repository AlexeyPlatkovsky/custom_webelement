# Writing Page Objects

This guide covers how to use the custom_webelement framework features when writing Page Object classes.

## Basic Setup

Replace Selenium's `PageFactory` with `iPageFactory` in your `AbstractPage` constructor:

```java
public abstract class AbstractPage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public AbstractPage() {
        this.driver = DriverFactory.initDriver();
        iPageFactory.initElements(this.driver, this);  // ← custom factory
        wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
    }
}
```

`iPageFactory` walks the full class hierarchy (including superclasses) to initialize all `@FindBy`-annotated fields.

---

## Declaring Elements

Use `iWebElement` instead of `WebElement` for all page fields:

```java
@FindBy(css = ".search-input")
private iWebElement searchInput;

@FindBy(xpath = "//button[@type='submit']")
private iWebElement submitButton;

@FindBy(css = ".result-item")
private iWebElementsList resultItems;
```

`iWebElement` implements the standard `WebElement` interface, so all existing Selenium methods work unchanged.

---

## Locator Templates

Use `%s` as a placeholder in a locator, then call `.template()` to produce a locator for a specific value. A single field covers many variations:

```java
@FindBy(xpath = "//button[text()='%s']")
public iWebElement button;

// Usage
button.template("OK").click();
button.template("Cancel").click();
button.template("Save").click();
```

Templates work with all locator types: `xpath`, `css`, `id`, `name`, `linkText`, `partialLinkText`, `tagName`, `className`.

> **Note:** `.template()` mutates the element's internal locator. It is not thread-safe if the same field is shared across threads. In parallel tests, declare template elements at the method level or use separate page instances per thread.

---

## Caching Elements

Add `@CacheElement` to elements that are stable for the lifetime of the page. The underlying `WebElement` reference is stored after the first lookup and reused on subsequent calls:

```java
@FindBy(css = ".gLFyf[type='search']")
@CacheElement
private iWebElement searchInput;
```

Use caching for:
- Elements that don't re-render (static headers, nav bars, stable form fields)
- Elements accessed many times in a single test

Do **not** cache elements that:
- Re-render dynamically (table rows, lazy-loaded lists)
- Appear/disappear based on state (modals, tooltips, validation messages)

Stale cached elements throw `StaleElementReferenceException`. If that happens, remove `@CacheElement` from that field.

---

## Custom Wait Timeouts

The default wait timeout for element presence is **5 seconds** (defined in `iWebElement`). Override per field with `@Waiter`:

```java
@FindBy(css = ".slow-loading-component")
@Waiter(waitFor = 15)
private iWebElement slowComponent;

@FindBy(css = ".instant-element")
@Waiter(waitFor = 1)
private iWebElement fastElement;
```

`waitFor` is in seconds. The default when `@Waiter` is absent is 5 seconds.

---

## Available iWebElement Methods

Beyond the standard `WebElement` interface, `iWebElement` provides:

### Interaction

| Method | Description |
|--------|-------------|
| `click()` | Click with 5s timeout; falls back to hidden-element wait on failure |
| `click(int timeout)` | Click with custom timeout in seconds |
| `sendText(CharSequence...)` | Type text into element |
| `sendKeys(Keys)` | Send keyboard key (e.g., `Keys.ENTER`) |
| `clear()` | Clear field via JS + native clear |
| `hover()` | Move mouse over element |
| `setFocus()` | Scroll into view and focus |
| `selectTextInElement()` | Select all text content via JS |
| `setCursorAtPosition(int)` | Place cursor at character position |
| `actions(Keys, char)` | Chord key combination (e.g., Ctrl+A) |

### Inspection

| Method | Description |
|--------|-------------|
| `getText()` | Returns inner text, falling back to `value` attribute |
| `getAttribute(String)` | Get any attribute |
| `getHref()` | Shorthand for `getAttribute("href")` |
| `hasChild(iWebElement)` | Returns `true` if the element has a matching child |
| `isScrollPresented()` | Returns `true` if element has a vertical scrollbar |
| `textIs(String)` | Wait for non-empty text, then compare |
| `isDisplayed()` | Returns `false` (not throws) if element is not visible |

### Navigation

| Method | Description |
|--------|-------------|
| `findElement(By)` | Returns child as `iWebElement` |
| `getChild(iWebElement)` | Returns child using another element's locator |
| `getChild(By)` | Returns child by `By` locator |
| `getChildren(iWebElement)` | Returns list of children matching another element's locator |
| `getParent()` | Returns parent element via `./..` XPath |

### Locator

| Method | Description |
|--------|-------------|
| `getLocator()` | Returns the `By` locator |
| `getLocatorValue()` | Returns locator value as plain string (without `By.xpath:` prefix) |
| `template(String)` | Substitutes `%s` in locator and returns `this` |

---

## Logging

All `iWebElement` actions are automatically logged at `DEBUG` level via `iLogger` (Log4j2). No configuration needed. Log output includes element name and locator for easy tracing.

Log level is configured in `src/main/resources/log4j2.xml`. Set to `DEBUG` for verbose element logs, `INFO` or `WARN` for quieter output.

---

## Element Highlighting

During test runs, interacted elements can be highlighted with a visible border. Enable in `webelement.properties`:

```properties
webelement.border.highlighted=true
webelement.border.width=3px
webelement.border.color=red
```

The highlight is cleared after each interaction (`stopHighlight()` is called in every method). Useful for visual debugging and recording demo GIFs.

---

## Full Page Object Example

```java
public class GooglePage extends AbstractPage {

    @FindBy(css = ".gLFyf[type='search']")
    @CacheElement
    private iWebElement searchInput;

    @FindBy(css = "input[value='Google Search']")
    private iWebElement searchButton;

    @FindBy(xpath = "//h3[contains(@class,'LC20lb')]")
    private iWebElementsList searchResults;

    @FindBy(xpath = "//a[text()='%s']")
    public iWebElement navLink;

    public GooglePage search(String query) {
        searchInput.clear();
        searchInput.sendText(query);
        searchButton.click();
        return this;
    }

    public List<String> getResultTitles() {
        return searchResults.stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public void clickNavLink(String linkText) {
        navLink.template(linkText).click();
    }
}
```
