package utils.logging;

import io.qameta.allure.Allure;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;

import java.io.ByteArrayInputStream;
import java.util.List;

public class TestListener extends TestListenerAdapter {
    private static final String SCREENSHOT_PATH_ATTRIBUTE = "testngScreenshotRelativePath";

    @Override
    public void onTestStart(ITestResult result) {
        ITestNGMethod method = result.getMethod();
        String description = method.getDescription();
        String testRailId = description == null ? "" : description.replaceAll("\\D+", "");
        iLogger.info("Start test " + method.getMethodName() + " with TestRailId " + testRailId);
        super.onTestStart(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!result.isSuccess()) {
            iLogger.error("Test failed", result.getThrowable());
            captureAndAttachScreenshot(result, "Failure screenshot");
        }
        attachTestLogs(result);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        attachTestLogs(tr);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        if (tr.getThrowable() != null) {
            iLogger.error("Test skipped", tr.getThrowable());
            captureAndAttachScreenshot(tr, "Skipped test screenshot");
        }
        attachTestLogs(tr);
        enrichSkippedThrowable(tr);
    }

    private void attachTestLogs(ITestResult result) {
        List<String> testLogs = Reporter.getOutput(result);
        if (testLogs == null || testLogs.isEmpty()) {
            return;
        }

        String normalizedLogs = normalizeLogs(testLogs);
        Object screenshotPath = result.getAttribute(SCREENSHOT_PATH_ATTRIBUTE);
        if (screenshotPath != null && !normalizedLogs.contains("Screenshot: " + screenshotPath)) {
            normalizedLogs = normalizedLogs + System.lineSeparator() + "Screenshot: " + screenshotPath;
        }

        Allure.addAttachment(
                "Execution log",
                "text/plain",
                normalizedLogs,
                ".log"
        );
    }

    private void captureAndAttachScreenshot(ITestResult result, String attachmentName) {
        try {
            iLogger.ScreenshotArtifact screenshot = iLogger.takeScreenshot();
            if (screenshot == null) {
                return;
            }

            result.setAttribute(SCREENSHOT_PATH_ATTRIBUTE, screenshot.reportRelativePath());
            Allure.addAttachment(
                    attachmentName,
                    "image/png",
                    new ByteArrayInputStream(screenshot.bytes()),
                    ".png"
            );
        } catch (Throwable throwable) {
            iLogger.error("Failed to capture screenshot for " + result.getName(), throwable);
        }
    }

    private void enrichSkippedThrowable(ITestResult result) {
        Throwable original = result.getThrowable();
        if (original == null) {
            return;
        }

        String originalMessage = original.getMessage();
        if (originalMessage != null && originalMessage.contains("Execution log:")) {
            return;
        }

        List<String> testLogs = Reporter.getOutput(result);
        String normalizedLogs = normalizeLogs(testLogs);
        if (normalizedLogs.isBlank()) {
            return;
        }

        StringBuilder enrichedMessage = new StringBuilder();
        enrichedMessage.append(originalMessage == null ? original.toString() : originalMessage)
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Execution log:")
                .append(System.lineSeparator())
                .append(normalizedLogs);

        SkipException enrichedThrowable = new SkipException(enrichedMessage.toString(), original);
        enrichedThrowable.setStackTrace(original.getStackTrace());
        result.setThrowable(enrichedThrowable);
    }

    private String normalizeLogs(List<String> testLogs) {
        if (testLogs == null || testLogs.isEmpty()) {
            return "";
        }

        return String.join(System.lineSeparator(), testLogs)
                .replaceAll("(?i)<br\\s*/?>", System.lineSeparator())
                .replace("</br>", System.lineSeparator())
                .replace("<pre>", "")
                .replace("</pre>", "")
                .replaceAll("(?i)<a\\s+[^>]*href=['\"]([^'\"]+)['\"][^>]*>.*?</a>", "Screenshot: $1")
                .replaceAll("(?i)<[^>]+>", "")
                .trim();
    }
}
