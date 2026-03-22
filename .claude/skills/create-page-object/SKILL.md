---
name: create-page-object
description: Scaffold a Page Object following project conventions. Use when creating a new page class or adding fields to an existing one.
---

## Location

Page Objects go in `src/test/java/pages/`. One class per logical page or component.

## Choose The Right Shape

Use one of these patterns:

1. **Top-level page** - navigable object, usually annotated with `@PageURL`, extends `iPage`.
2. **Page component** - reusable nested section, can also extend `iPage`, and may use a class-level `@FindBy` to scope child locators.

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
3. Stable CSS class or attribute selector.
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

### Logging
- Use `iLogger`; never `System.out.println`.
