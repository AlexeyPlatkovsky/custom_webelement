package unit_tests.utils.logging;

import org.mockito.Mockito;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.ResultMap;
import org.testng.xml.XmlSuite;
import utils.assertions.iAssert;
import utils.logging.CustomArtifactsReporter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Test(groups = {"unit"}, singleThreaded = true)
public class CustomArtifactsReporterTest {

    @AfterMethod(alwaysRun = true)
    public void cleanupReporter() {
        Reporter.clear();
        Reporter.setCurrentTestResult(null);
    }

    @Test
    public void generateReportShouldRenderMethodStatusScreenshotAndLog() throws Exception {
        ITestResult failedResult = mockResult("tests.SmokeTest", "openPageTest", ITestResult.FAILURE, "screenshots/failure.png");
        Reporter.setCurrentTestResult(failedResult);
        Reporter.log("2026-03-07 22:10:10.000: ERROR: Something bad happened</br>");
        Reporter.log("<br><a href='screenshots/failure.png' target='_blank'> CLICK TO SEE SCREENSHOT </a></br>");
        Reporter.setCurrentTestResult(null);

        ITestResult passedResult = mockResult("tests.SmokeTest", "searchTest", ITestResult.SUCCESS, null);
        Reporter.setCurrentTestResult(passedResult);
        Reporter.log("2026-03-07 22:10:20.000: INFO: Business step</br>");
        Reporter.log("2026-03-07 22:10:20.100: DEBUG: Internal details</br>");
        Reporter.setCurrentTestResult(null);

        ITestContext context = Mockito.mock(ITestContext.class);
        ResultMap failedMap = new ResultMap();
        failedMap.addResult(failedResult, failedResult.getMethod());
        ResultMap passedMap = new ResultMap();
        passedMap.addResult(passedResult, passedResult.getMethod());
        Mockito.when(context.getFailedTests()).thenReturn(failedMap);
        Mockito.when(context.getSkippedTests()).thenReturn(new ResultMap());
        Mockito.when(context.getPassedTests()).thenReturn(passedMap);

        var suiteResult = Mockito.mock(org.testng.ISuiteResult.class);
        Mockito.when(suiteResult.getTestContext()).thenReturn(context);

        var suite = Mockito.mock(org.testng.ISuite.class);
        Mockito.when(suite.getName()).thenReturn("Demo Suite");
        Mockito.when(suite.getResults()).thenReturn(Maps.newHashMap(Collections.singletonMap("Demo Test", suiteResult)));
        Mockito.when(suite.getXmlSuite()).thenReturn(new XmlSuite());

        Path outputDir = Files.createTempDirectory("custom-artifacts-report");
        new CustomArtifactsReporter().generateReport(Collections.emptyList(), List.of(suite), outputDir.toString());

        Path reportFile = outputDir.resolve("custom-artifacts.html");
        iAssert.isTrue(Files.exists(reportFile), "custom-artifacts.html should be generated");

        String html = Files.readString(reportFile);
        iAssert.contains(html, "Demo Suite", "report contains suite name");
        iAssert.contains(html, "tests.SmokeTest", "report contains class name");
        iAssert.contains(html, "openPageTest", "report contains failed method name");
        iAssert.contains(html, "FAILED", "report contains failed status");
        String expectedScreenshotHref = outputDir.resolve("screenshots/failure.png").toUri().toString();
        iAssert.contains(html, "href='" + expectedScreenshotHref + "'", "report contains screenshot href");
        iAssert.contains(html, "Something bad happened", "report contains failure details");
        iAssert.contains(html, "Business step", "report contains info log from passed test");
        iAssert.notContains(html, "Internal details", "DEBUG logs should be hidden for passed tests");
    }

    private ITestResult mockResult(String className, String methodName, int status, String screenshotPath) {
        ITestResult result = Mockito.mock(ITestResult.class);
        ITestNGMethod testMethod = Mockito.mock(ITestNGMethod.class);
        ITestClass testClass = Mockito.mock(ITestClass.class);

        Mockito.when(result.getMethod()).thenReturn(testMethod);
        Mockito.when(result.getTestClass()).thenReturn(testClass);
        Mockito.when(result.getStatus()).thenReturn(status);
        Mockito.when(result.getAttribute("testngScreenshotRelativePath")).thenReturn(screenshotPath);

        Mockito.when(testMethod.getMethodName()).thenReturn(methodName);
        Mockito.when(testClass.getName()).thenReturn(className);
        return result;
    }
}
