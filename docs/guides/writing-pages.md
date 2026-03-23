# Writing Pages

Use this guide when creating or updating Page Objects and component classes in this framework.

## Page Basics

- Use `@PageURL` on page classes that can be opened directly.
- Use `@FindBy` on `iWebElement`, `iWebElementsList`, and component fields.
- Prefer CSS locators for normal lookup and interaction.
- Use XPath only when CSS cannot express the locator clearly.

## `iWebElementsList`

- `iWebElementsList` is read-only. Mutation-style `List` methods are unsupported.
- CSS-based lists resolve items by their current DOM index, which avoids the old `:nth-child(...)` mismatch.
- XPath-based lists keep an item-specific XPath locator for item-scoped actions.

Example:

```java
@FindBy(css = ".result-item")
private iWebElementsList resultItems;

@FindBy(xpath = "//*[@id='reactive-result-list']//li[contains(@class,'reactive-result-item')]")
private iWebElementsList xpathReactiveResultItems;
```

## `selectTextInElement()`

- `iWebElement.selectTextInElement()` only supports XPath locators.
- Calling it on an element created from a CSS, id, name, or other non-XPath locator throws `UnsupportedOperationException`.
- If you need text selection behavior, declare that element with `@FindBy(xpath = "...")`.

Example:

```java
@FindBy(xpath = "//*[@id='reactive-result-list']//li[contains(@class,'reactive-result-item')]")
private iWebElementsList xpathReactiveResultItems;

public String captureReactiveItemSelection(int index) {
    xpathReactiveResultItems.get(index).selectTextInElement();
    return selectionOutput.getText();
}
```

## Components

- Class-level `@FindBy` scopes all nested fields in the component.
- A field-level `@FindBy` on the component field overrides the class-level scope when both are present.
- Keep component locators local to the component instead of duplicating page-wide selectors.
