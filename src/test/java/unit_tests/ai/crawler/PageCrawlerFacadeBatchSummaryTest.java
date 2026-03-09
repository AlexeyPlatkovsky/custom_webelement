package unit_tests.ai.crawler;

import ai.crawler.PageCrawlerFacade;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = "unit")
public class PageCrawlerFacadeBatchSummaryTest {

    @Test
    public void logBatchSummaryDoesNotThrowForAllSucceeded() {
        PageCrawlerFacade.logBatchSummary(
            List.of("LoginPage <- https://example.com/login",
                    "DashboardPage <- https://example.com/dashboard"),
            List.of()
        );
    }

    @Test
    public void logBatchSummaryDoesNotThrowForAllFailed() {
        PageCrawlerFacade.logBatchSummary(
            List.of(),
            List.of("https://example.com/login [FAILED: timeout]",
                    "https://example.com/dashboard [FAILED: connection refused]")
        );
    }

    @Test
    public void logBatchSummaryDoesNotThrowForMixedResults() {
        PageCrawlerFacade.logBatchSummary(
            List.of("LoginPage <- https://example.com/login"),
            List.of("https://example.com/checkout [FAILED: timeout after 30000ms]")
        );
    }

    @Test
    public void logBatchSummaryDoesNotThrowForEmptyBatch() {
        PageCrawlerFacade.logBatchSummary(List.of(), List.of());
    }

    @Test
    public void logBatchSummaryDoesNotThrowForSingleUrl() {
        PageCrawlerFacade.logBatchSummary(
            List.of("LoginPage <- https://example.com/login"),
            List.of()
        );
    }
}
