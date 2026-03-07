package tests;

import com.sun.net.httpserver.HttpServer;
import core.driver.DriverFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.LocalPage;
import utils.properties.SystemProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * UI smoke test that starts a local HTTP server and exercises the full
 * Selenium + iWebElement stack without requiring any external internet access.
 *
 * Serves a minimal HTML page on a random OS-assigned port on 127.0.0.1,
 * then drives Chrome (headless) against it and verifies iWebElement
 * locators, attribute reading, text extraction and visibility checks.
 */
@Test(groups = {"ui"}, singleThreaded = true)
public class LocalServerTest extends BaseTest {

    private static final String HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head><title>Local Test Page</title></head>
            <body>
              <h1 id="main-heading">Hello from local server</h1>
              <p class="description">iWebElement smoke test</p>
              <a id="test-link" href="https://github.com">GitHub Link</a>
            </body>
            </html>
            """;

    private HttpServer server;

    @BeforeClass(alwaysRun = true)
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] body = HTML.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        SystemProperties.ROOT_URL = "http://127.0.0.1:" + port;
    }

    /**
     * All tests in this class share one browser session (singleThreaded = true).
     * Suppress the per-method driver quit from BaseTest so the session survives
     * across methods; we quit it once here when the class is finished.
     */
    @Override
    protected void tearDown(ITestResult result) {
        // intentionally no-op between methods
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        SystemProperties.ROOT_URL = "";
        // Quit the shared browser session now that all methods are done
        DriverFactory.getCurrentDriver().quit();
    }

    @Test(description = "Page title is returned correctly by driver.getTitle()")
    public void checkPageTitleTest() {
        LocalPage page = new LocalPage();
        page.openPage();
        Assert.assertEquals(page.getPageTitle(), "Local Test Page",
                "Page title mismatch");
    }

    @Test(description = "iWebElement.getText() returns the h1 heading text")
    public void checkMainHeadingTextTest() {
        LocalPage page = new LocalPage();
        page.openPage();
        Assert.assertEquals(page.getMainHeadingText(), "Hello from local server",
                "Main heading text mismatch");
    }

    @Test(description = "iWebElement.getText() returns the paragraph description")
    public void checkDescriptionTextTest() {
        LocalPage page = new LocalPage();
        page.openPage();
        Assert.assertEquals(page.getDescriptionText(), "iWebElement smoke test",
                "Description text mismatch");
    }

    @Test(description = "iWebElement.isDisplayed() returns true for a visible link")
    public void checkTestLinkVisibilityTest() {
        LocalPage page = new LocalPage();
        page.openPage();
        Assert.assertTrue(page.isTestLinkVisible(), "Test link should be visible");
    }

    @Test(description = "iWebElement.getAttribute() reads the href attribute correctly")
    public void checkTestLinkHrefTest() {
        LocalPage page = new LocalPage();
        page.openPage();
        // getAttribute("href") returns the resolved URL; Chrome may add a trailing slash
        Assert.assertTrue(page.getTestLinkHref().startsWith("https://github.com"),
                "Link href attribute should start with 'https://github.com' but was: " + page.getTestLinkHref());
    }
}
