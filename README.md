# custom_webelement

Open-source UI test framework built on Java 21, Selenium 4, and TestNG. The core difference from plain Selenium is that element interactions are wrapped in `iWebElement` / `iWebElementsList` and logged by default, while `iPageFactory` initializes pages, nested components, and framework-specific annotations.

## Highlights

- Drop-in `iPageFactory` replacement for Selenium `PageFactory`
- `iWebElement` and `iWebElementsList` wrappers with built-in action logging
- Recursive initialization for nested `iPage` components
- `@CacheElement` and `@Waiter` support for common UI test patterns
- `@PageURL` hierarchy support for both relative app pages and absolute external-site page groups
- Default `iPage.isOpened()` URL matching, with optional landmark checks in concrete pages
- TestNG integration with Allure and custom artifact reporting
- AI-assisted test authoring workflow via `ai-write-test` and Playwright CLI

## Quick Start

Replace the default `PageFactory` call with `iPageFactory`:

```java
// Before
public AbstractPage(WebDriver driver) {
    this.driver = driver;
    PageFactory.initElements(this.driver, this);
}

// After
public AbstractPage(WebDriver driver) {
    this.driver = driver;
    iPageFactory.initElements(this.driver, this);
}
```

See [`docs/guides/getting-started.md`](docs/guides/getting-started.md) for the full integration walkthrough.

## URL Resolution

`iPage.openPage()` and the default `iPage.isOpened()` resolve URLs from the full `@PageURL` class hierarchy.

- If the hierarchy is fully relative, the framework concatenates the segments and prepends `base_url`.
- If any class in the hierarchy has an absolute `@PageURL`, that value becomes the root and `base_url` is ignored for that page chain.

Relative page resolved through `base_url`:

```java
@PageURL("/component-playground.html")
public class ComponentPlaygroundPage extends iPage { }
```

```bash
./gradlew test -Ddriver=chrome -Dbase_url=http://localhost:8080 --tests "tests.ComponentPlaygroundPageTest"
```

Absolute base page with relative children:

```java
@PageURL("https://practicetestautomation.com")
public abstract class PracticeTestAutomationBasePage extends iPage { }

@PageURL("/practice/")
public class PracticePage extends PracticeTestAutomationBasePage { }
```

This resolves to `https://practicetestautomation.com/practice/` even if `base_url` is set for other tests in the same run.

## AI-Assisted Test Authoring

The repo includes an `ai-write-test` workflow for creating, editing, and repairing tests under `src/test/`. It reads existing page objects, uses Playwright CLI for live DOM capture when new locators are needed, and keeps framework changes out of `src/main`.

- Start here: [`docs/guides/create-test-with-ai.md`](docs/guides/create-test-with-ai.md)
- Playwright CLI setup: [`docs/guides/playwright-cli.md`](docs/guides/playwright-cli.md)
- Test case template: [`docs/guides/test-case-template.md`](docs/guides/test-case-template.md)

## Guides

| Guide | What it covers |
|---|---|
| [Getting Started](docs/guides/getting-started.md) | Integration, driver setup, running tests |
| [Writing Pages](docs/guides/writing-pages.md) | Page objects, components, locator conventions |
| [iWebElement API](docs/guides/webelement-api.md) | Element methods, `@CacheElement`, `@Waiter`, template locators |
| [Assertions](docs/guides/assertions.md) | `iAssert` methods and soft assertion usage |
| [Logging and Reports](docs/guides/logging.md) | Log levels, report policy, screenshots, listener setup |
| [Configuration](docs/guides/configuration.md) | System properties, URL resolution, highlighting, remote execution |
| [Create Test with AI](docs/guides/create-test-with-ai.md) | `ai-write-test` workflow, scope, and prerequisites |
| [Playwright CLI Setup](docs/guides/playwright-cli.md) | Browser automation setup, CLI permissions, auth state handling |
| [Test Case Template](docs/guides/test-case-template.md) | Input format for new AI-authored tests |

## Project Layout

- `src/main/java/` - framework code
- `src/test/java/pages/` - demo page objects and components
- `src/test/java/tests/` - UI tests and local fixture base classes
- `src/test/java/unit_tests/` - unit tests for framework behavior
- `docs/guides/` - user-facing setup and usage guides

## Contributor Guidelines

- Read the relevant guide and affected code before changing framework behavior.
- Prefer small, local changes over broad cleanup.
- Keep locators in page objects or components, not in test classes.
- Update docs when setup, configuration, or workflow expectations change.
- For shared framework refactors, add or adjust a protecting test first.
- Run the smallest meaningful validation before considering the work complete.

## Validation

For fast local verification:

```bash
./gradlew compileJava
./gradlew compileTestJava
./gradlew checkstyleMain
./gradlew checkstyleTest
./gradlew test -Dsuite=unit
```

For the local UI fixture:

```bash
./gradlew test -Dsuite=ui -Ddriver=chrome --tests "tests.ComponentPlaygroundPageTest"
```

## Demo Coverage

Example page objects and tests are in `src/test/java/`:

- `pages/ComponentPlaygroundPage.java` - page object covering lists, caching, and nested components
- `pages/PracticeTestAutomationBasePage.java` and child pages - absolute `@PageURL` base-page pattern for external sites
- `pages/*DemoComponent.java` - component scope examples for class-level and field-level `@FindBy`
- `tests/ComponentPlaygroundPageTest.java` - local fixture UI tests for caching, rerendering, selection, and component scoping
- `tests/PracticeTestAutomationLoginTest.java` - external-site UI flow using inherited `@PageURL` values

See [`AGENTS.md`](AGENTS.md) for the project-specific working rules used by coding agents and contributors.
