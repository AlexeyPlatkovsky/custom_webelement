# Step 2.1 — `PageSnapshot` Value Object

**Phase:** 2 — PageCrawler
**Status:** Todo
**Depends on:** —
**Blocks:** Steps 2.3, 3.2

---

## Goal

Define the immutable data structure that `PageCrawler` populates and `PageObjectGenerator`
consumes. `PageSnapshot` is the handoff between the crawling layer and the generation layer.

---

## File

`src/main/java/ai/crawler/PageSnapshot.java`

---

## Implementation

```java
package ai.crawler;

import lombok.Value;

@Value
public class PageSnapshot {

    /** The URL that was crawled. */
    String url;

    /** The page `<title>` at time of snapshot. */
    String title;

    /**
     * Cleaned HTML: scripts, styles, and comments removed; structural noise trimmed;
     * truncated at 50,000 characters.
     */
    String cleanedHtml;

    /**
     * Accessibility tree as a JSON string produced by Playwright's
     * {@code page.accessibility().snapshot()}.
     */
    String accessibilityTree;

    /**
     * Base64-encoded PNG screenshot. {@code null} unless
     * {@link PageCrawler#crawlWithScreenshot(String)} was called.
     */
    String screenshotBase64;
}
```

---

## Notes

- All fields are non-null except `screenshotBase64` (nullable by design).
- Lombok `@Value` provides the all-args constructor, getters, `equals`, `hashCode`, and
  `toString`. No manual code needed.
- `accessibilityTree` is a raw JSON string (not a parsed object) to keep the snapshot
  serialization-friendly and avoid coupling to a specific JSON library in the data class.

---

## Definition of Done

- [ ] File created in `src/main/java/ai/crawler/`
- [ ] `./gradlew compileJava` passes
