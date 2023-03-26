package utils.logging;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import utils.properties.SystemProperties;

public class TestListener extends TestListenerAdapter {

    @Override
    public void onTestStart(ITestResult result) {
        ITestNGMethod method = result.getMethod();
        String link = SystemProperties.TEST_RAIL_URL + method.getDescription().replaceAll("\\D+", "");
        iLogger.info("Start test " + method.getMethodName() + " with TestRailId " + method.getDescription());
        iLogger.info("<a href=\"" + link + "\">Visit Test Rail</a>");
        super.onTestStart(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!result.isSuccess()) {
            iLogger.error("Test failed", result.getThrowable());
            iLogger.takeScreenshot();
        }
    }
}

