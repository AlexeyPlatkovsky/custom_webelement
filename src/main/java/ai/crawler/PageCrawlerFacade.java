package ai.crawler;

import utils.logging.iLogger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PageCrawlerFacade {

    private PageCrawlerFacade() {}

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
