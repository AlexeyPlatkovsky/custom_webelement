# custom_webelement

Open-source UI test framework built on Java 21, Selenium 4, and TestNG. The core difference from plain Selenium is that element interactions are wrapped in `iWebElement` / `iWebElementsList` and logged by default, while `iPageFactory` initializes pages, nested components, and framework-specific annotations.

## Highlights

- Drop-in `iPageFactory` replacement for Selenium `PageFactory`
- `iWebElement` and `iWebElementsList` wrappers with built-in action logging
- Recursive initialization for nested `iPage` components
- `@CacheElement` and `@Waiter` support for common UI test patterns
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
| [Configuration](docs/guides/configuration.md) | System properties, highlighting, remote execution |
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
- `pages/*DemoComponent.java` - component scope examples for class-level and field-level `@FindBy`
- `tests/ComponentPlaygroundPageTest.java` - local fixture UI tests for caching, rerendering, selection, and component scoping

See [`AGENTS.md`](AGENTS.md) for the project-specific working rules used by coding agents and contributors.
