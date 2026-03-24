# custom_webelement

Open-source framework for automating UI tests using Java 21 and Selenium. The primary distinction from native Selenium is built-in logging for all actions by default — finding elements, clicking, sending keys, and more.

## Quick Start

Replace the default `PageFactory` call with `iPageFactory`:

```java
public AbstractPage() {
    this.driver = DriverFactory.initDriver();
    iPageFactory.initElements(this.driver, this); // custom PageFactory
    wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
}
```

See [`docs/guides/getting-started.md`](docs/guides/getting-started.md) for the full integration walkthrough.

## Guides

| Guide | What it covers |
|---|---|
| [Getting Started](docs/guides/getting-started.md) | Integration, driver setup, running tests |
| [Writing Pages](docs/guides/writing-pages.md) | Page objects, components, locator conventions |
| [iWebElement API](docs/guides/webelement-api.md) | All element methods, `@CacheElement`, `@Waiter`, template locators |
| [Assertions](docs/guides/assertions.md) | `iAssert` methods, soft assertions with `assertAll` |
| [Logging and Reports](docs/guides/logging.md) | Log levels, report policy, screenshots, listener setup |
| [Configuration](docs/guides/configuration.md) | System properties, element highlight, remote execution |

## Validation

For fast local verification:

```bash
./gradlew compileJava
./gradlew compileTestJava
./gradlew checkstyleMain
./gradlew checkstyleTest
./gradlew test -Dsuite=unit
```

## Demo Tests

Example page objects and tests are in `src/test/java/`:

- `pages/` — sample page and component classes
- `tests/DuckDuckGoPageTest.java` — UI test examples
- `tests/ComposedDuckDuckGoPageTest.java` — nested component example

Feel free to use and contribute to this open-source framework.
