# Step 2.4 — `PageCrawlerFacade` (crawl-only)

**Phase:** 2 — PageCrawler
**Status:** Todo
**Depends on:** Step 2.3
**Blocks:** Step 3.5 (full wiring adds generation on top of this)

---

## Goal

Provide the public, static API for crawling one or more URLs. This step implements only
the crawling half — generation is wired in Step 3.5. Keeping them separate makes the
crawler independently testable.

---

## File

`src/main/java/ai/crawler/PageCrawlerFacade.java`

---

## Implementation

```java
package ai.crawler;

import utils.logging.iLogger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PageCrawlerFacade {

    private PageCrawlerFacade() {}   // static utility class

    /**
     * Crawls a single URL and returns its snapshot.
     *
     * @throws PageCrawlerException if the page cannot be loaded
     * @throws IllegalArgumentException if the URL is malformed or uses a non-http(s) scheme
     */
    public static PageSnapshot crawl(String url) {
        validateUrl(url);
        try (PageCrawler crawler = new PageCrawler()) {
            return crawler.crawl(url);
        }
    }

    /**
     * Crawls all provided URLs sequentially using a single browser instance.
     * Failed URLs are skipped and logged; the batch continues.
     *
     * @return snapshots in the same order as the input list;
     *         entries for failed URLs are absent (list may be shorter than input)
     */
    public static List<PageSnapshot> crawl(List<String> urls) {
        List<PageSnapshot> results = new ArrayList<>();
        try (PageCrawler crawler = new PageCrawler()) {
            for (String url : urls) {
                try {
                    validateUrl(url);
                    results.add(crawler.crawl(url));
                } catch (Exception e) {
                    iLogger.error("PageCrawlerFacade: failed to crawl '" + url + "' — " + e.getMessage());
                }
            }
        }
        return results;
    }

    private static void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new IllegalArgumentException(
                    "URL must use http or https scheme, got: " + url
                );
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
    }
}
```

---

## Design Notes

- **Single browser for batch:** one `PageCrawler` (one Playwright + Browser) serves the
  entire batch. Each URL still gets its own isolated `BrowserContext` inside `crawler.crawl()`.
  This is more efficient than spawning a new browser per URL.
- **Partial batch results:** the method returns only successful snapshots. Callers that need
  to know which URLs failed should compare the result list size to the input list size, or
  wrap in their own error-tracking loop.
- **Thread safety:** both methods are safe to call from a single thread. Do not call
  `crawl(List<String>)` from multiple threads concurrently — Playwright is not thread-safe.
  See edge case 5.9 in the main `README.md`.

---

## Unit Test

**File:** `src/test/java/unit_tests/ai/crawler/PageCrawlerFacadeTest.java`

Test cases (using local HTTP server from Step 2.3 test helper):
- Single URL → returns `PageSnapshot` with correct URL
- Batch of 3 URLs → returns list of 3 snapshots in order
- Batch where one URL is unreachable → 2 snapshots returned, error logged, no exception thrown
- Malformed URL (no scheme) → `IllegalArgumentException`
- Non-http URL (`ftp://...`) → `IllegalArgumentException`

---

## Definition of Done

- [ ] `PageCrawlerFacade.crawl(String)` and `crawl(List<String>)` implemented
- [ ] URL validation rejects non-http(s) and malformed URLs
- [ ] Batch mode: one failed URL does not abort the rest
- [ ] Single browser instance used for entire batch
- [ ] `PageCrawler` closed via try-with-resources in both methods
- [ ] All unit tests pass
