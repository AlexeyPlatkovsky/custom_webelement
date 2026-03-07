package unit_tests.utils.logging;

import org.mockito.Mockito;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import utils.assertions.iAssert;
import utils.logging.iLogger;

import java.util.List;

@Test(groups = {"unit"}, singleThreaded = true)
public class ILoggerReportPolicyTest {

    @AfterMethod(alwaysRun = true)
    public void cleanupReporterState() {
        iLogger.clearDebugBuffer();
        iLogger.setLogOnlyInfo(true);
        Reporter.clear();
        Reporter.setCurrentTestResult(null);
    }

    @Test
    public void debugShouldBeHiddenForSuccessfulFlowUntilFlushed() {
        ITestResult result = Mockito.mock(ITestResult.class);
        Reporter.setCurrentTestResult(result);
        iLogger.setLogOnlyInfo(true);

        iLogger.debug("Hidden debug line");
        List<String> beforeFlush = Reporter.getOutput(result);
        iAssert.isFalse(
                beforeFlush.stream().anyMatch(line -> line.contains("DEBUG: Hidden debug line")),
                "Debug log should not be reported immediately when only-info mode is enabled"
        );

        iLogger.flushDebugBufferToReporter();
        List<String> afterFlush = Reporter.getOutput(result);
        iAssert.isTrue(
                afterFlush.stream().anyMatch(line -> line.contains("DEBUG: Hidden debug line")),
                "Debug log should be available after explicit flush"
        );
    }
}
