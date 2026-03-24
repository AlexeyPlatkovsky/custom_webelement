# iWebElement API

`iWebElement` wraps Selenium's `WebElement` and adds automatic logging, waiting, caching, and template locators.

## Core Interactions

| Method | Log level | Notes |
|---|---|---|
| `click()` | INFO | Waits up to 5 s for the element to be clickable; retries via JS fallback |
| `click(int timeout)` | INFO | Custom wait timeout in seconds |
| `sendKeys(CharSequence...)` | INFO | Delegates to native `sendKeys` after resolving the element |
| `sendKeys(Keys)` | INFO | Sends a keyboard key using `Actions` |
| `sendText(CharSequence...)` | — | Sends keys without an INFO log; use when logging is handled upstream |
| `clear()` | INFO | Clears value via JS then native `clear()` |
| `submit()` | INFO | Submits the enclosing form |
| `hover()` | — | Moves the mouse to the element using `Actions` |

## State Checks

| Method | Returns | Notes |
|---|---|---|
| `isDisplayed()` | `boolean` | Returns `false` on exception (e.g. stale element) |
| `isEnabled()` | `boolean` | DEBUG log |
| `isSelected()` | `boolean` | DEBUG log |
| `hasText()` | — | Use `getText()` and check for blank; `textIs(String)` waits for text then compares |
| `textIs(String)` | `boolean` | Waits until element has non-empty text, then compares |
| `hasChild(iWebElement)` | `boolean` | Checks whether a child element exists in the DOM |
| `isScrollPresented()` | `boolean` | Checks `scrollHeight > clientHeight` via JS |

## Content Access

| Method | Notes |
|---|---|
| `getText()` | Returns inner text; falls back to `value` attribute if text is blank |
| `getAttribute(String)` | DEBUG log |
| `getHref()` | Shorthand for `getAttribute("href")` |
| `getCssValue(String)` | DEBUG log |

## Navigation and Focus

| Method | Notes |
|---|---|
| `setFocus()` | Scrolls element into view and focuses it |
| `getParent()` | Returns the DOM parent as an `iWebElement` |
| `findElement(By)` | Returns child as `iWebElement` |
| `getChild(iWebElement)` | Finds a child by the given element's locator |
| `getChild(By)` | Finds a child by the given `By` locator |

## Text Selection and Cursor

| Method | Constraint |
|---|---|
| `selectTextInElement()` | XPath locators only; throws `UnsupportedOperationException` for other strategies |
| `setCursorAtPosition(int)` | Positions text cursor at a character offset via JS |
| `actions(Keys, char)` | Performs a key-chord (e.g. Ctrl+A) using `Actions` |

## Template Locators

Use `%s` as a placeholder in `@FindBy` values, then call `.template(value)` before the action.

```java
@FindBy(xpath = "//button[text()='%s']")
public iWebElement button;

button.template("OK").click();
button.template("SAVE").click();
```

All locator strategies are supported. The original locator is preserved between calls.

## Annotations

### `@CacheElement`

Stores the resolved `WebElement` after the first lookup. Subsequent calls skip the DOM search.

```java
@FindBy(css = ".gLFyf[type='search']")
@CacheElement
private iWebElement searchInput;
```

Use for stable, always-present elements. Avoid for elements that are recreated by the application.

### `@Waiter`

Overrides the default 5-second element wait for a single field.

```java
@FindBy(css = ".loading-spinner")
@Waiter(waitFor = 15)
private iWebElement spinner;
```

Default is 1 second when `@Waiter` is present without a value. Omitting the annotation uses the 5-second default.

## Timeout Diagnostics

When an element wait times out, `iWebElement` automatically logs:
- How many elements matched the locator at the moment of failure
- Current page URL and title
- `document.readyState`
- First 1200 characters of normalized page source

These appear as `ERROR` entries and are visible in reports for failed tests.

## See Also

- [iWebElementsList](writing-pages.md#iwebelementslist) for working with collections
- [Writing Pages](writing-pages.md) for locator and component conventions
