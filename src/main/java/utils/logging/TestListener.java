package utils.logging;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import utils.properties.SystemProperties;

public class TestListener extends TestListenerAdapter {

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
            try {
                iLogger.takeScreenshot();
            } catch (Throwable throwable) {
                iLogger.error("Failed to capture screenshot", throwable);
            }
        }
    }
}

