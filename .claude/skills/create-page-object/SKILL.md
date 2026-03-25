---
name: create-page-object
description: Scaffold a Page Object following project conventions. Use when creating a new page class or adding fields to an existing one.
---

## Location

Page Objects go in `src/test/java/pages/`. One class per logical page or component.

## Choose The Right Shape

Use one of these patterns:

1. **Top-level page** - navigable object, annotated with `@PageURL`, extends `iPage` or a site base page.
2. **Site base page** - abstract anchor for a group of pages that share a domain; holds the root `@PageURL` so child pages use relative paths.
3. **Page component** - reusable nested section, can also extend `iPage`, and may use a class-level `@FindBy` to scope child locators.

## URL Hierarchy

The framework builds page URLs by walking up the class chain and concatenating each class's `@PageURL` value, then prepending `Environment.getRootUrl()` (`base_url` property).

**Rule: when two or more pages share a domain, use a base page — not repeated absolute URLs.**

```java
// Base page — holds the domain; abstract because it is never instantiated directly
@PageURL("https://example.com")
public abstract class ExampleBasePage extends iPage { }

// Child pages — only know their own path segment
@PageURL("/search/")
public class SearchPage extends ExampleBasePage { ... }   // → https://example.com/search/

@PageURL("/results/")
public class ResultsPage extends ExampleBasePage { ... }  // → https://example.com/results/
```

Changing the domain is then a one-line edit in the base class.

**When to use an absolute URL directly on a leaf page** (no base class needed):
- The page is the only page from that domain in the entire test suite.
- The URL must not be affected by `base_url` under any circumstances.

**Important:** the `base_url` property (`Environment.getRootUrl()`) is always prepended after the hierarchy is assembled. If `base_url` is non-empty it will be prepended to the assembled path — including the domain in the base class — producing a broken URL. Use the base-class pattern only when `base_url` is empty (the default) or equals the same domain.

## Top-Level Page Template

```java
package pages;

import core.web.annotations.PageURL;
import core.web.iPage;
import core.web.iWebElement;
import core.web.iWebElementsList;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

@PageURL("https://example.com")
public class MyPage extends iPage {
    @FindBy(id = "search-input")
    private iWebElement searchInput;

    @FindBy(css = ".result-item")
    private iWebElementsList resultItems;

    public void searchForText(String searchText) {
        iLogger.info("Search text '{}'", searchText);
        searchInput.sendKeys(searchText);
        searchInput.sendKeys(Keys.ENTER);
    }
}
```

## Component Template

```java
package pages;

import core.web.iPage;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;

@FindBy(css = "main")
public class SearchComponent extends iPage {
    @FindBy(css = "textarea[name='q'], input[name='q']")
    private iWebElement searchInput;
}
```

## Rules

### Types
- Use `iWebElement`, not `WebElement`.
- Use `iWebElementsList` for collections, not `List<WebElement>`.
- For normal pages and components, extend `iPage` and let the framework initialize fields.
- Do not use Selenium `PageFactory`.

### Locator Priority
1. `id` - most stable, prefer when available.
2. `data-testid`, `name`, or `aria-*` attributes - semantic and stable.
3. Stable CSS class or attribute selector. When the selector relies on a utility or framework-generated class (e.g., WordPress block classes, Tailwind/Bootstrap utilities), always scope it with a stable semantic ancestor (e.g., `.post-content .has-text-align-center strong`, not `.has-text-align-center strong`).
4. XPath - last resort only.

Avoid fragile locators: positional XPath (`//div[3]`), auto-generated classes, text-content selectors that break on copy changes.

### Field Naming
Name fields by their UI role, not their HTML tag:

| Element | Good name | Avoid |
|---|---|---|
| Search input | `searchInput` | `inputField`, `textBox1` |
| Submit button | `submitButton` | `btn`, `button1` |
| Error text | `errorMessage` | `label`, `span` |
| Result rows | `resultItems` | `elements`, `list` |

### Behavior
- Keep assertions and `@Test` methods out of Page Objects. They belong in test classes.
- Do not call `driver.findElement()` directly inside a Page Object.
- Do not use `Thread.sleep()`; use `@Waiter` or explicit waits via the framework.
- Use `@CacheElement` only on stable, non-dynamic elements.
- Prefer page methods that describe business actions (`searchForText`, `openFilters`, `hasSearchResults`) rather than low-level driver mechanics.
- For top-level pages, use `@PageURL` when the page can be opened directly.
- **Navigation methods are always `void`.** A method that clicks a link or button must not return the destination page object — it just performs the action. The test is responsible for constructing the next page. This keeps POs decoupled: the current page does not need to know about, or import, the destination class.
- **Every top-level navigable page must implement `isOpened()`** — a `boolean` method that returns `true` when the page is fully loaded and in the expected state (typically: correct URL fragment + a key landmark element is displayed). Components that extend `iPage` are exempt. The test should call `isOpened()` on each page before interacting with it.

### Logging
- Use `iLogger`; never `System.out.println`.
