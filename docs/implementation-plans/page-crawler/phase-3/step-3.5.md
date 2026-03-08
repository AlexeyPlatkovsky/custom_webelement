# Step 3.5 — `PageCrawlerFacade` Full Wiring

**Phase:** 3 — Page Object Generator
**Status:** Todo
**Depends on:** Steps 2.4, 3.2, 3.4
**Blocks:** Phase 4 (polish)

---

## Goal

Extend `PageCrawlerFacade` with the `generatePageObject` / `generatePageObjects` public API
that orchestrates the full pipeline: crawl → generate → write. This is the entry point
developers call to produce Page Objects.

---

## File

`src/main/java/ai/crawler/PageCrawlerFacade.java` — extend the crawl-only version from Step 2.4

---

## Additional Methods

```java
package ai.crawler;

import ai.AiProviderFactory;
import ai.generator.GeneratedPageObject;
import ai.generator.PageObjectGenerator;
import ai.generator.PageObjectWriter;
import ai.provider.AiProvider;
import utils.logging.iLogger;

import java.util.ArrayList;
import java.util.List;

public class PageCrawlerFacade {

    // ── existing crawl-only methods from Step 2.4 ────────────────────────────

    public static PageSnapshot crawl(String url) { ... }
    public static List<PageSnapshot> crawl(List<String> urls) { ... }

    // ── new generation methods ────────────────────────────────────────────────

    /**
     * Crawls the URL, generates a Page Object via the configured AI provider,
     * and writes it to src/test/java/generated/.
     */
    public static void generatePageObject(String url) {
        generatePageObjects(List.of(url));
    }

    /**
     * Crawls each URL sequentially, generates Page Objects, and writes them.
     * A failure on one URL is logged and skipped; the batch continues.
     * A batch summary is logged after all URLs are processed.
     */
    public static void generatePageObjects(List<String> urls) {
        AiProvider provider       = AiProviderFactory.create();
        PageObjectGenerator gen   = new PageObjectGenerator(provider);
        PageObjectWriter writer   = new PageObjectWriter();

        List<String> succeeded = new ArrayList<>();
        List<String> failed    = new ArrayList<>();

        try (PageCrawler crawler = new PageCrawler()) {
            for (String url : urls) {
                try {
                    validateUrl(url);
                    iLogger.info("PageCrawlerFacade: crawling " + url);
                    PageSnapshot snapshot = crawler.crawl(url);
                    GeneratedPageObject po = gen.generate(snapshot);
                    writer.write(po);
                    succeeded.add(po.getClassName() + " <- " + url);
                } catch (Exception e) {
                    iLogger.error("PageCrawlerFacade: failed for '" + url + "' — " + e.getMessage());
                    failed.add(url + " [FAILED: " + e.getMessage() + "]");
                }
            }
        }

        logBatchSummary(succeeded, failed);
    }
}
```

### `logBatchSummary`

```java
private static void logBatchSummary(List<String> succeeded, List<String> failed) {
    int total = succeeded.size() + failed.size();
    iLogger.info(String.format(
        "PageCrawlerFacade: batch complete — %d/%d page objects generated",
        succeeded.size(), total
    ));
    succeeded.forEach(s -> iLogger.info("  OK  " + s));
    failed.forEach(f   -> iLogger.error("  ERR " + f));
}
```

---

## Developer Usage Example

```java
// Single URL
PageCrawlerFacade.generatePageObject("https://example.com/login");
// → writes src/test/java/generated/LoginPage.java

// Multiple URLs in one browser session
PageCrawlerFacade.generatePageObjects(List.of(
    "https://example.com/login",
    "https://example.com/dashboard",
    "https://example.com/checkout"
));
// → writes LoginPage.java, DashboardPage.java, CheckoutPage.java
// → logs batch summary
```

---

## Integration Test

**File:** `src/test/java/unit_tests/ai/crawler/PageCrawlerFacadeTest.java` — add to existing file

Test cases (mock `AiProvider` returning a fixed Page Object source):
- `generatePageObject(url)` → `.java` file created in `generated/`
- `generatePageObjects(List.of(url1, bad_url))` → one file written, one error logged,
  no exception escapes to the caller
- Batch of 3 → 3 files written, batch summary logged
- Verify batch summary mentions all three class names

---

## Definition of Done

- [ ] `generatePageObject(String)` and `generatePageObjects(List<String>)` added to facade
- [ ] Full pipeline wired: `AiProviderFactory → PageObjectGenerator → PageObjectWriter`
- [ ] One browser instance per `generatePageObjects` call
- [ ] Batch summary logged after all URLs processed
- [ ] Exception on one URL does not abort the batch
- [ ] Integration test with mocked `AiProvider` passes
- [ ] `./gradlew compileJava` passes
