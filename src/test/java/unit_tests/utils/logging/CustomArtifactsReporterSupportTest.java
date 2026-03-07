package unit_tests.utils.logging;

import org.testng.annotations.Test;
import org.testng.ITestResult;
import utils.assertions.iAssert;
import utils.logging.CustomArtifactsReporterSupport;

import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;

@Test(groups = {"unit"})
public class CustomArtifactsReporterSupportTest {

    @Test
    public void normalizeReporterLogsShouldReplaceScreenshotAnchorWithPath() {
        String normalized = CustomArtifactsReporterSupport.normalizeReporterLogs(List.of(
                "2026-03-07 22:00:00.000: INFO: Start</br>",
                "<br><a href='screenshots/example.png' target='_blank'> CLICK TO SEE SCREENSHOT </a></br>",
                "<pre>stack line 1\nstack line 2</pre>"
        ));

        iAssert.contains(normalized, "INFO: Start", "normalized logs contain info line");
        iAssert.contains(normalized, "Screenshot: screenshots/example.png", "normalized logs contain screenshot path");
        iAssert.contains(normalized, "stack line 1", "normalized logs keep preformatted stack line");
        iAssert.notContains(normalized, "<a ", "normalized logs remove anchor tags");
    }

    @Test
    public void normalizeReporterLogsShouldDecodeCommonHtmlEntities() {
        String normalized = CustomArtifactsReporterSupport.normalizeReporterLogs(List.of(
                "<pre>Driver info: host=&quot;demo&quot;, value=&lt;ok&gt;, quote=&#39;x&#39;</pre>"
        ));

        iAssert.contains(normalized, "host=\"demo\"", "normalized logs decode quoted host");
        iAssert.contains(normalized, "value=<ok>", "normalized logs decode angle brackets");
        iAssert.contains(normalized, "quote='x'", "normalized logs decode single quote entity");
    }

    @Test
    public void resolveScreenshotPathShouldPreferAttributeOverLogs() {
        Optional<String> actual = CustomArtifactsReporterSupport.resolveScreenshotPath(
                "screenshots/from-attribute.png",
                List.of("<a href='screenshots/from-log.png' target='_blank'> CLICK TO SEE SCREENSHOT </a>")
        );

        iAssert.isTrue(actual.isPresent(), "screenshot path is resolved from attribute");
        iAssert.equalsTo(actual.get(), "screenshots/from-attribute.png", "attribute path has priority");
    }

    @Test
    public void resolveScreenshotPathShouldFallbackToLogsWhenAttributeMissing() {
        Optional<String> actual = CustomArtifactsReporterSupport.resolveScreenshotPath(
                null,
                List.of("<a href='screenshots/from-log.png' target='_blank'> CLICK TO SEE SCREENSHOT </a>")
        );

        iAssert.isTrue(actual.isPresent(), "screenshot path is resolved from logs");
        iAssert.equalsTo(actual.get(), "screenshots/from-log.png", "log path is used when attribute is missing");
    }

    @Test
    public void resolveScreenshotPathShouldExtractDataImageWhenProvided() {
        Optional<String> actual = CustomArtifactsReporterSupport.resolveScreenshotPath(
                null,
                List.of("<a href='data:image/png;base64,abc123'> CLICK TO SEE SCREENSHOT </a>")
        );

        iAssert.isTrue(actual.isPresent(), "data image screenshot path is resolved");
        iAssert.isTrue(actual.get().startsWith("data:image/png;base64,"), "resolved screenshot path is data URI");
    }

    @Test
    public void resolveScreenshotPathShouldBeEmptyWhenMissingEverywhere() {
        Optional<String> actual = CustomArtifactsReporterSupport.resolveScreenshotPath(
                null,
                List.of("No screenshot here")
        );

        iAssert.isTrue(actual.isEmpty(), "screenshot path is empty when it is missing everywhere");
    }

    @Test
    public void renderScreenshotCellShouldReturnClickableAnchor() {
        Path outputDir;
        try {
            outputDir = Files.createTempDirectory("artifacts-report");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String html = CustomArtifactsReporterSupport.renderScreenshotCell(Optional.of("screenshots/file.png"), outputDir);
        iAssert.contains(html, "href='screenshots/file.png'", "rendered screenshot cell contains relative href");
        iAssert.contains(html, "Open Screenshot", "rendered screenshot cell contains open link label");
    }

    @Test
    public void htmlEscapeShouldEscapeUnsafeCharacters() {
        String escaped = CustomArtifactsReporterSupport.htmlEscape("<script>alert('x')</script>");
        iAssert.notEqualsTo(escaped, "<script>alert('x')</script>", "html escaping changes unsafe string");
        iAssert.contains(escaped, "&lt;script&gt;", "escaped output contains escaped script tag");
    }

    @Test
    public void filterLogsForStatusShouldKeepOnlyInfoAndScreenshotForPassedTests() {
        String filtered = CustomArtifactsReporterSupport.filterLogsForStatus(
                String.join(System.lineSeparator(),
                        "2026-03-08 10:00:00.000: INFO: Step one",
                        "2026-03-08 10:00:00.100: DEBUG: Internal trace",
                        "Screenshot: screenshots/pass.png"
                ),
                ITestResult.SUCCESS
        );

        iAssert.contains(filtered, "INFO: Step one", "successful filter keeps info logs");
        iAssert.contains(filtered, "Screenshot: screenshots/pass.png", "successful filter keeps screenshot line");
        iAssert.notContains(filtered, "DEBUG: Internal trace", "successful filter removes debug logs");
    }

    @Test
    public void filterLogsForStatusShouldKeepFullLogsForFailedTests() {
        String filtered = CustomArtifactsReporterSupport.filterLogsForStatus(
                String.join(System.lineSeparator(),
                        "2026-03-08 10:00:00.000: INFO: Step one",
                        "2026-03-08 10:00:00.100: DEBUG: Internal trace"
                ),
                ITestResult.FAILURE
        );

        iAssert.contains(filtered, "INFO: Step one", "failed filter keeps info logs");
        iAssert.contains(filtered, "DEBUG: Internal trace", "failed filter keeps debug logs");
    }
}
