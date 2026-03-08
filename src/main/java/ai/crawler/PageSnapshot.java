package ai.crawler;

import lombok.Value;

@Value
public class PageSnapshot {

    /** The URL that was crawled. */
    String url;

    /** The page {@code <title>} at time of snapshot. */
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
