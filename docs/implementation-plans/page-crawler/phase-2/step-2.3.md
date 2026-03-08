# Step 2.3 — `PageCrawler`

**Phase:** 2 — PageCrawler
**Status:** Todo
**Depends on:** Steps 2.1, 2.2, 0.2
**Blocks:** Step 2.4 (`PageCrawlerFacade` wraps `PageCrawler`)

---

## Goal

Implement the Playwright-based web crawler that loads a URL in a headless Chromium browser,
cleans the DOM, captures the accessibility tree, and returns a `PageSnapshot`.

---

## File

`src/main/java/ai/crawler/PageCrawler.java`

---

## Implementation

```java
package ai.crawler;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import utils.logging.iLogger;

import java.util.Base64;

public class PageCrawler implements AutoCloseable {

    private static final int PAGE_LOAD_TIMEOUT_MS = 30_000;

    private final Playwright playwright;
    private final Browser browser;

    public PageCrawler() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    public PageSnapshot crawl(String url) {
        return crawlInternal(url, false);
    }

    public PageSnapshot crawlWithScreenshot(String url) {
        return crawlInternal(url, true);
    }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }
}
```

### Internal crawl logic

```java
private PageSnapshot crawlInternal(String url, boolean captureScreenshot) {
    BrowserContext context = browser.newContext();
    try {
        Page page = context.newPage();

        try {
            page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
                .setTimeout(PAGE_LOAD_TIMEOUT_MS));
        } catch (PlaywrightException e) {
            throw new PageCrawlerException("Failed to load URL: " + url, e);
        }

        String title       = page.title();
        String rawHtml     = page.content();
        String cleanedHtml = DomCleaner.clean(rawHtml);

        AccessibilityNode snapshot = page.accessibility().snapshot();
        String accessibilityTree  = snapshot != null
            ? AccessibilitySerializer.toJson(snapshot)
            : "{}";

        String screenshotBase64 = null;
        if (captureScreenshot) {
            byte[] bytes = page.screenshot();
            screenshotBase64 = Base64.getEncoder().encodeToString(bytes);
        }

        iLogger.info(String.format(
            "PageCrawler: crawled '%s' | title='%s' | html=%d chars | a11y=%d chars",
            url, title, cleanedHtml.length(), accessibilityTree.length()
        ));

        return new PageSnapshot(url, title, cleanedHtml, accessibilityTree, screenshotBase64);

    } finally {
        context.close();
    }
}
```

### `AccessibilitySerializer`

Playwright returns a tree of `AccessibilityNode` objects. Serialize to JSON using Jackson:

```java
// ai/crawler/AccessibilitySerializer.java
class AccessibilitySerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static String toJson(AccessibilityNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            iLogger.warn("AccessibilitySerializer: failed to serialize node — " + e.getMessage());
            return "{}";
        }
    }
}
```

> Package-private (`class`, not `public class`) — only used by `PageCrawler`.

### `PageCrawlerException`

```java
// ai/crawler/PageCrawlerException.java
public class PageCrawlerException extends RuntimeException {
    public PageCrawlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## Configuration Defaults

| Setting | Default | Property (future) |
|---------|---------|-------------------|
| Browser | Chromium headless | — |
| Page load wait | `NETWORKIDLE` | — |
| Timeout | 30,000 ms | `ai.crawler.timeout-ms` |

---

## Integration Test

**File:** `src/test/java/unit_tests/ai/crawler/PageCrawlerTest.java`

Use `com.sun.net.httpserver.HttpServer` (built into JDK — no extra dep) to serve a local
HTML page and verify:

- `snapshot.getUrl()` equals the requested URL
- `snapshot.getTitle()` matches the `<title>` tag in the test HTML
- `snapshot.getCleanedHtml()` does not contain `<script>` tags
- `snapshot.getAccessibilityTree()` is valid JSON (parseable by Jackson)
- `snapshot.getScreenshotBase64()` is null for `crawl()`, non-null for `crawlWithScreenshot()`
- Requesting a non-existent URL throws `PageCrawlerException`

Tag this test `@Test(groups = "integration")` so it is excluded from unit-only runs.

---

## Definition of Done

- [ ] `PageCrawler` implements `AutoCloseable`
- [ ] `crawl()` and `crawlWithScreenshot()` both populate `PageSnapshot` correctly
- [ ] Each URL uses an isolated `BrowserContext` (no cookie/session bleed between crawls)
- [ ] `PageCrawlerException` wraps Playwright timeout/navigation errors
- [ ] `context.close()` called in `finally` even on error
- [ ] `browser.close()` + `playwright.close()` called in `close()`
- [ ] Integration test passes with local HTTP server
