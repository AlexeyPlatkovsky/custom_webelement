package unit_tests.ai.crawler;

import ai.crawler.PageCrawlerFacade;
import ai.crawler.PageSnapshot;
import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "integration")
public class PageCrawlerFacadeTest {

    private static final String PAGE_HTML = "<html><head><title>Page</title></head><body><p>Content</p></body></html>";

    private HttpServer server;
    private String baseUrl;

    @BeforeMethod
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        for (String path : List.of("/page1", "/page2", "/page3")) {
            server.createContext(path, exchange -> {
                byte[] response = PAGE_HTML.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });
        }
        server.start();
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterMethod
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void singleUrlReturnedWithCorrectUrl() {
        PageSnapshot snapshot = PageCrawlerFacade.crawl(baseUrl + "/page1");
        assertEquals(snapshot.getUrl(), baseUrl + "/page1");
    }

    @Test
    public void batchOfThreeUrlsReturnsThreeSnapshots() {
        List<String> urls = List.of(
            baseUrl + "/page1",
            baseUrl + "/page2",
            baseUrl + "/page3"
        );
        List<PageSnapshot> results = PageCrawlerFacade.crawl(urls);
        assertEquals(results.size(), 3, "all three snapshots should be returned");
    }

    @Test
    public void batchWithOneUnreachableUrlReturnsTwoSnapshotsWithoutThrowing() {
        List<String> urls = List.of(
            baseUrl + "/page1",
            "http://localhost:1/unreachable",
            baseUrl + "/page3"
        );
        List<PageSnapshot> results = PageCrawlerFacade.crawl(urls);
        assertEquals(results.size(), 2, "only successful snapshots should be in the result");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void malformedUrlNoSchemeThrowsIllegalArgumentException() {
        PageCrawlerFacade.crawl("not-a-url");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nonHttpSchemeThrowsIllegalArgumentException() {
        PageCrawlerFacade.crawl("ftp://example.com/path");
    }
}
