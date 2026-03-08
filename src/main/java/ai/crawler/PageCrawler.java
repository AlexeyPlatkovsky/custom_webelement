package ai.crawler;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
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

            String title = page.title();
            String rawHtml = page.content();
            String cleanedHtml = DomCleaner.clean(rawHtml);
            String accessibilityTree = AccessibilitySerializer.extract(page);

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
}
