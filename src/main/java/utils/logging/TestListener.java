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

    @Override
    public void onTestStart(ITestResult result) {
        iLogger.clearDebugBuffer();
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
            iLogger.flushDebugBufferToReporter();
            captureAndAttachScreenshot(result, "Failure screenshot");
        }
        attachTestLogs(result);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        attachTestLogs(tr);
        iLogger.clearDebugBuffer();
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        if (tr.getThrowable() != null) {
            iLogger.error("Test skipped", tr.getThrowable());
            iLogger.flushDebugBufferToReporter();
            captureAndAttachScreenshot(tr, "Skipped test screenshot");
        }
        attachTestLogs(tr);
        enrichSkippedThrowable(tr);
        iLogger.clearDebugBuffer();
    }

    private void attachTestLogs(ITestResult result) {
        List<String> testLogs = Reporter.getOutput(result);
        if (testLogs == null || testLogs.isEmpty()) {
            return;
        }

        String normalizedLogs = CustomArtifactsReporterSupport.normalizeReporterLogs(testLogs);
        Object screenshotPath = result.getAttribute(CustomArtifactsReporterSupport.SCREENSHOT_PATH_ATTRIBUTE);
        if (screenshotPath != null && !normalizedLogs.contains("Screenshot: " + screenshotPath)) {
            normalizedLogs = normalizedLogs + System.lineSeparator() + "Screenshot: " + screenshotPath;
        }
        String reportLogs = CustomArtifactsReporterSupport.filterLogsForStatus(normalizedLogs, result.getStatus());
        if (reportLogs.isBlank()) {
            return;
        }

        Allure.addAttachment(
                "Execution log",
                "text/plain",
                reportLogs,
                ".log"
        );
    }

    private void captureAndAttachScreenshot(ITestResult result, String attachmentName) {
        try {
            iLogger.ScreenshotArtifact screenshot = iLogger.takeScreenshot();
            if (screenshot == null) {
                return;
            }

            result.setAttribute(CustomArtifactsReporterSupport.SCREENSHOT_PATH_ATTRIBUTE, screenshot.reportRelativePath());
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
        String normalizedLogs = CustomArtifactsReporterSupport.normalizeReporterLogs(testLogs);
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
}
