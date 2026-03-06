# PageCrawler

`PageCrawler` uses Playwright Java to visit a URL and extract a structured representation of the page — DOM and accessibility tree — for use as AI prompt context.

## Why Playwright, Not Selenium

The existing framework uses Selenium, but `PageCrawler` uses Playwright Java for three reasons:

1. **Accessibility tree access** — Playwright exposes `page.accessibility().snapshot()` natively. Selenium has no equivalent.
2. **Isolated crawl context** — crawling runs in a separate Playwright browser instance and does not interfere with the active Selenium WebDriver session.
3. **Simpler headless config** — Playwright handles headless Chromium setup with no WebDriverManager dependency.

See [ADR-001](../architecture/decisions.md#adr-001-use-playwright-java-for-domaccessibility-tree-extraction) for the full decision rationale.

## What PageCrawler Extracts

### DOM Snapshot

Cleaned HTML of the page body:

- Script, style, and comment nodes removed
- Inline event handlers removed
- Empty structural elements (divs/spans with no text, no role, no id) trimmed
- Truncated at ~50,000 characters to fit prompt budgets

### Accessibility Tree

JSON snapshot of the browser's accessibility tree via `page.accessibility().snapshot()`. Includes:

- Role (button, textbox, heading, link, …)
- Name / label
- Value (for inputs)
- `focusable`, `checked`, `expanded`, `required` states
- Nesting (children)

The accessibility tree is often more useful than raw DOM for element identification because it reflects what screen readers and AI agents "see".

## Output Format

`PageCrawler.crawl(url)` returns a `PageSnapshot` object:

```java
public class PageSnapshot {
    private String url;
    private String title;
    private String cleanedHtml;          // trimmed DOM
    private String accessibilityTree;    // JSON string
}
```

`AiTestGenerator` passes both `cleanedHtml` and `accessibilityTree` to the AI provider in the user message.

## Playwright Dependency

Add to `build.gradle`:

```groovy
implementation 'com.microsoft.playwright:playwright:1.44.0'
```

First run downloads Playwright browser binaries (~150MB). For CI environments with no internet access, pre-download via:

```bash
./gradlew run -PmainClass=com.microsoft.playwright.CLI --args="install chromium"
```

Or set `PLAYWRIGHT_BROWSERS_PATH` to a pre-populated directory.

## Configuration

No dedicated config file. `PageCrawler` uses sensible defaults:

| Setting | Default | Description |
|---------|---------|-------------|
| Browser | Chromium (headless) | Fixed; not configurable in MVP |
| Page load timeout | 30s | Wait for `networkidle` |
| DOM size limit | 50,000 chars | Truncated before prompt construction |
| Screenshot | Disabled | Enable by calling `crawlWithScreenshot(url)` |

## Usage

```java
PageCrawler crawler = new PageCrawler();
PageSnapshot snapshot = crawler.crawl("https://example.com/login");

// snapshot.getCleanedHtml()       → trimmed DOM
// snapshot.getAccessibilityTree() → JSON a11y tree
// snapshot.getTitle()             → page title
```

With screenshot (for vision-capable providers):

```java
PageSnapshot snapshot = crawler.crawlWithScreenshot("https://example.com/login");
// snapshot.getScreenshotBase64() → base64 PNG
```

## Limitations

- Static crawl only. `PageCrawler` loads the page and takes a snapshot. It does not interact with the page (no login, no form fill). Pages requiring authentication must be handled by providing pre-authenticated cookies or session state.
- JavaScript-heavy SPAs: `PageCrawler` waits for `networkidle` but cannot guarantee that all lazy-loaded content is visible. For complex SPAs, consider adding a custom wait before snapshot.
- Captcha and bot-detection: some pages block headless Chromium. There is no built-in bypass.
