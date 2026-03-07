package utils.logging;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CustomArtifactsReporter implements IReporter {
    private static final String REPORT_FILE_NAME = "custom-artifacts.html";

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<ArtifactEntry> entries = collectEntries(suites);
        entries.sort(
                Comparator.comparingInt((ArtifactEntry entry) -> CustomArtifactsReporterSupport.statusSortOrder(entry.status()))
                        .thenComparing(ArtifactEntry::suiteName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ArtifactEntry::testName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ArtifactEntry::className, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ArtifactEntry::methodName, String.CASE_INSENSITIVE_ORDER)
        );

        Path reportFile = Path.of(outputDirectory, REPORT_FILE_NAME);
        String html = render(entries, Path.of(outputDirectory));
        try {
            Files.createDirectories(reportFile.getParent());
            Files.writeString(reportFile, html, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create custom TestNG artifacts report: " + reportFile, e);
        }
    }

    List<ArtifactEntry> collectEntries(List<ISuite> suites) {
        List<ArtifactEntry> entries = new ArrayList<>();
        for (ISuite suite : suites) {
            String suiteName = safeString(suite.getName());
            Map<String, ISuiteResult> results = suite.getResults();
            for (Map.Entry<String, ISuiteResult> suiteEntry : results.entrySet()) {
                String testName = safeString(suiteEntry.getKey());
                ITestContext testContext = suiteEntry.getValue().getTestContext();
                addByStatus(entries, suiteName, testName, testContext.getFailedTests().getAllResults(), ITestResult.FAILURE);
                addByStatus(entries, suiteName, testName, testContext.getSkippedTests().getAllResults(), ITestResult.SKIP);
                addByStatus(entries, suiteName, testName, testContext.getPassedTests().getAllResults(), ITestResult.SUCCESS);
            }
        }
        return entries;
    }

    private void addByStatus(List<ArtifactEntry> entries, String suiteName, String testName, Set<ITestResult> testResults, int fallbackStatus) {
        for (ITestResult testResult : testResults) {
            int status = testResult.getStatus() > 0 ? testResult.getStatus() : fallbackStatus;
            String className = testResult.getTestClass() == null ? "UnknownClass" : safeString(testResult.getTestClass().getName());
            String methodName = testResult.getMethod() == null ? safeString(testResult.getName()) : safeString(testResult.getMethod().getMethodName());
            List<String> rawLogs = Reporter.getOutput(testResult);
            String normalizedLogs = CustomArtifactsReporterSupport.normalizeReporterLogs(rawLogs);
            String reportLogs = CustomArtifactsReporterSupport.filterLogsForStatus(normalizedLogs, status);
            Optional<String> screenshotPath = CustomArtifactsReporterSupport.resolveScreenshotPath(
                    testResult.getAttribute(CustomArtifactsReporterSupport.SCREENSHOT_PATH_ATTRIBUTE),
                    rawLogs
            );

            entries.add(new ArtifactEntry(suiteName, testName, className, methodName, status, screenshotPath, reportLogs));
        }
    }

    private String render(List<ArtifactEntry> entries, Path reportOutputDir) {
        String generatedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='utf-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
                .append("<title>TestNG Artifacts Report</title>")
                .append("<style>")
                .append("body{font-family:Segoe UI,Arial,sans-serif;margin:16px;background:#f5f7fa;color:#1f2937;}")
                .append("h1{margin:0 0 4px 0;font-size:26px;} .meta{margin-bottom:16px;color:#4b5563;}")
                .append("table{width:100%;border-collapse:collapse;background:#fff;}")
                .append("th,td{border:1px solid #d1d5db;padding:10px;vertical-align:top;font-size:13px;}")
                .append("th{background:#e5e7eb;text-align:left;} tr.failed td{background:#fee2e2;} tr.skipped td{background:#fef3c7;}")
                .append("tr.passed td{background:#dcfce7;} .status{font-weight:700;} .missing{color:#6b7280;}")
                .append("pre{margin:0;white-space:pre-wrap;word-break:break-word;font-size:12px;line-height:1.4;}")
                .append(".hint{color:#4b5563;margin-top:4px;word-break:break-all;font-size:12px;}")
                .append("</style></head><body>");

        html.append("<h1>TestNG Artifacts Report</h1>")
                .append("<div class='meta'>Generated at ")
                .append(CustomArtifactsReporterSupport.htmlEscape(generatedAt))
                .append(" | Total tests: ")
                .append(entries.size())
                .append("</div>");

        html.append("<table><thead><tr>")
                .append("<th>Suite</th><th>Test</th><th>Class</th><th>Method</th><th>Status</th><th>Screenshot</th><th>Execution Log</th>")
                .append("</tr></thead><tbody>");

        if (entries.isEmpty()) {
            html.append("<tr><td colspan='7'><span class='missing'>No test results were found.</span></td></tr>");
        } else {
            for (ArtifactEntry entry : entries) {
                String statusLabel = CustomArtifactsReporterSupport.statusLabel(entry.status());
                String rowClass = statusLabel.toLowerCase();
                html.append("<tr class='").append(rowClass).append("'>")
                        .append("<td>").append(CustomArtifactsReporterSupport.htmlEscape(entry.suiteName())).append("</td>")
                        .append("<td>").append(CustomArtifactsReporterSupport.htmlEscape(entry.testName())).append("</td>")
                        .append("<td>").append(CustomArtifactsReporterSupport.htmlEscape(entry.className())).append("</td>")
                        .append("<td>").append(CustomArtifactsReporterSupport.htmlEscape(entry.methodName())).append("</td>")
                        .append("<td><span class='status'>").append(statusLabel).append("</span></td>")
                        .append("<td>").append(CustomArtifactsReporterSupport.renderScreenshotCell(entry.screenshotPath(), reportOutputDir)).append("</td>")
                        .append("<td><pre>").append(CustomArtifactsReporterSupport.htmlEscape(entry.executionLog())).append("</pre></td>")
                        .append("</tr>");
            }
        }

        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    record ArtifactEntry(
            String suiteName,
            String testName,
            String className,
            String methodName,
            int status,
            Optional<String> screenshotPath,
            String executionLog
    ) {
    }
}
