# Logging and Test Reports

## Log Levels

Use `iLogger` for all test and framework logging. Do not use `System.out.println`.

| Level | When to use |
|---|---|
| `INFO` | Business-visible steps and user journey actions |
| `DEBUG` | Technical diagnostics that do not represent user flow |
| `ERROR` | Failures, unexpected states, exceptions |

### Methods

```java
iLogger.info("Navigate to checkout");
iLogger.info("Cart contains {} items", String.valueOf(count));

iLogger.debug("Element locator resolved: {}", locator);

iLogger.error("Failed to load widget: {}", message);
iLogger.error("Exception during click", throwable);
```

Use `{}` as the placeholder in parameterized overloads.

## Built-in INFO Logs

These framework actions already emit INFO entries — no extra logging needed in page or test code:

- `iWebElement.click()`
- `iWebElement.sendKeys(...)`
- `iWebElement.submit()`
- `iWebElement.clear()`
- `iAssert.*` — every assertion
- `iPage.openPage()` — when you call it in your page class

## Report Log Policy

The log policy applies per test result:

| Test outcome | What appears in the report log |
|---|---|
| Passed | INFO lines only |
| Failed | Full log: INFO + DEBUG + ERROR |
| Skipped | Full log: INFO + DEBUG + ERROR |

This keeps passing test reports concise while preserving diagnostic detail when something goes wrong.

The policy is applied to:
1. The Allure `Execution log` attachment.
2. The custom TestNG artifacts report at `build/reports/tests/testng/custom-artifacts.html`.

## Screenshots

Screenshots are captured automatically on test failure and on skip (when a throwable is present). They are attached to:
- The Allure report as a `Failure screenshot` attachment.
- The custom artifacts HTML report via a link in the TestNG reporter output.

To take a screenshot manually from a test or page method:

```java
iLogger.ScreenshotArtifact screenshot = iLogger.takeScreenshot();
```

Screenshots are saved to `build/reports/tests/testng/screenshots/`.

## Registering the Listener

`TestListener` must be registered in your TestNG suite for log capture and screenshot attachment to work.

```xml
<listeners>
    <listener class-name="utils.logging.TestListener"/>
</listeners>
```

## TestRail Integration

`TestListener` extracts the TestRail case ID from the `@Test(description = "...")` annotation value (digits only). The ID is logged at test start. No additional configuration is required beyond setting the correct description.

## Element Highlight

During test execution, resolved elements are visually highlighted in the browser. Configure this in `webelement.properties` — see [Configuration](configuration.md#element-highlight).
