package ai.crawler;

import ai.generator.GeneratedPageObject;
import ai.generator.PageObjectGenerator;
import ai.generator.PageObjectWriter;
import ai.provider.AiProvider;
import utils.logging.iLogger;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Single entry point for all page-crawling and Page Object generation operations.
 *
 * <h2>Crawling</h2>
 * <pre>{@code
 * PageSnapshot snap = PageCrawlerFacade.crawl("https://example.com/login");
 * List<PageSnapshot> snaps = PageCrawlerFacade.crawl(List.of(url1, url2));
 * }</pre>
 *
 * <h2>Generation from existing snapshot</h2>
 * <pre>{@code
 * GeneratedPageObject po = PageCrawlerFacade.generatePageObject(snapshot, provider);
 * Path file = PageCrawlerFacade.generateAndWrite(snapshot, provider);
 * }</pre>
 *
 * <h2>Full pipeline (crawl + generate + write)</h2>
 * <pre>{@code
 * Path file = PageCrawlerFacade.generateAndWrite("https://example.com/login", provider);
 * }</pre>
 */
public class PageCrawlerFacade {

    private PageCrawlerFacade() {}

    // ── Crawling ──────────────────────────────────────────────────────────────

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

    // ── Page Object generation ─────────────────────────────────────────────────

    /**
     * Generates a Page Object Java class from an already-crawled {@link PageSnapshot}.
     * Does not write any files.
     *
     * @param snapshot the page data to use as generation input
     * @param provider the AI provider to call
     * @return generated Page Object containing class name and full source code
     */
    public static GeneratedPageObject generatePageObject(PageSnapshot snapshot, AiProvider provider) {
        return new PageObjectGenerator(provider).generate(snapshot);
    }

    /**
     * Crawls {@code url} and generates a Page Object from the resulting snapshot.
     * Does not write any files.
     *
     * @param url      page URL to crawl (must use http or https)
     * @param provider AI provider to use for generation
     * @return generated Page Object containing class name and full source code
     * @throws PageCrawlerException     if the page cannot be crawled
     * @throws IllegalArgumentException if the URL is malformed or uses a non-http(s) scheme
     */
    public static GeneratedPageObject generatePageObject(String url, AiProvider provider) {
        PageSnapshot snapshot = crawl(url);
        return generatePageObject(snapshot, provider);
    }

    /**
     * Generates a Page Object from an existing snapshot and writes it to
     * {@code src/test/java/generated/<ClassName>.java}.
     *
     * @param snapshot the page data to use as generation input
     * @param provider the AI provider to call
     * @return path of the written {@code .java} file
     */
    public static Path generateAndWrite(PageSnapshot snapshot, AiProvider provider) {
        GeneratedPageObject pageObject = generatePageObject(snapshot, provider);
        return new PageObjectWriter().write(pageObject);
    }

    /**
     * Full pipeline: crawls {@code url}, generates a Page Object, and writes it to
     * {@code src/test/java/generated/<ClassName>.java}.
     *
     * @param url      page URL to crawl
     * @param provider AI provider to use for generation
     * @return path of the written {@code .java} file
     * @throws PageCrawlerException     if the page cannot be crawled
     * @throws IllegalArgumentException if the URL is malformed or uses a non-http(s) scheme
     */
    public static Path generateAndWrite(String url, AiProvider provider) {
        PageSnapshot snapshot = crawl(url);
        return generateAndWrite(snapshot, provider);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

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

