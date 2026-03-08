package unit_tests.ai.crawler;

import ai.crawler.PageCrawler;
import ai.crawler.PageCrawlerException;
import ai.crawler.PageSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "integration")
public class PageCrawlerTest {

    private static final String TEST_HTML = "<!DOCTYPE html>"
        + "<html><head><title>Test Page</title></head>"
        + "<body><h1>Hello World</h1>"
        + "<script>alert('should be removed')</script>"
        + "<p>Paragraph content</p>"
        + "</body></html>";

    private HttpServer server;
    private String baseUrl;
    private PageCrawler crawler;

    @BeforeMethod
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", exchange -> {
            byte[] response = TEST_HTML.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
        crawler = new PageCrawler();
    }

    @AfterMethod
    public void stopServer() {
        if (crawler != null) {
            crawler.close();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void snapshotUrlMatchesRequestedUrl() {
        PageSnapshot snapshot = crawler.crawl(baseUrl + "/test");
        assertEquals(snapshot.getUrl(), baseUrl + "/test");
    }

    @Test
    public void snapshotTitleMatchesTitleTag() {
        PageSnapshot snapshot = crawler.crawl(baseUrl + "/test");
        assertEquals(snapshot.getTitle(), "Test Page");
    }

    @Test
    public void cleanedHtmlDoesNotContainScriptTags() {
        PageSnapshot snapshot = crawler.crawl(baseUrl + "/test");
        assertNotNull(snapshot.getCleanedHtml());
        assertTrue(!snapshot.getCleanedHtml().contains("<script"),
            "cleanedHtml should not contain script tags");
    }

    @Test
    public void accessibilityTreeIsValidJson() throws Exception {
        PageSnapshot snapshot = crawler.crawl(baseUrl + "/test");
        assertNotNull(snapshot.getAccessibilityTree());
        // Verify it's parseable JSON
        new ObjectMapper().readTree(snapshot.getAccessibilityTree());
    }

    @Test
    public void screenshotIsNullForCrawl() {
        PageSnapshot snapshot = crawler.crawl(baseUrl + "/test");
        assertNull(snapshot.getScreenshotBase64(), "screenshot should be null for crawl()");
    }

    @Test
    public void screenshotIsNonNullForCrawlWithScreenshot() {
        PageSnapshot snapshot = crawler.crawlWithScreenshot(baseUrl + "/test");
        assertNotNull(snapshot.getScreenshotBase64(), "screenshot should be non-null for crawlWithScreenshot()");
        assertTrue(snapshot.getScreenshotBase64().length() > 0, "screenshot should not be empty");
    }

    @Test(expectedExceptions = PageCrawlerException.class)
    public void throwsPageCrawlerExceptionForUnreachableUrl() {
        crawler.crawl("http://localhost:1");
    }
}
