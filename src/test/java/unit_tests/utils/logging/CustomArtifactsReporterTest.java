package unit_tests.utils.logging;

import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.internal.ResultMap;
import org.testng.xml.XmlSuite;
import utils.assertions.iAssert;
import utils.logging.CustomArtifactsReporter;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        ITestResult passedResult = mockResult("tests.SmokeTest", "searchTest", ITestResult.SUCCESS, null);

        ITestContext context = Mockito.mock(ITestContext.class);
        ResultMap failedMap = new ResultMap();
        failedMap.addResult(failedResult);
        ResultMap passedMap = new ResultMap();
        passedMap.addResult(passedResult);
        Mockito.when(context.getFailedTests()).thenReturn(failedMap);
        Mockito.when(context.getSkippedTests()).thenReturn(new ResultMap());
        Mockito.when(context.getPassedTests()).thenReturn(passedMap);

        var suiteResult = Mockito.mock(org.testng.ISuiteResult.class);
        Mockito.when(suiteResult.getTestContext()).thenReturn(context);

        ISuite suite = suite("Demo Suite", Map.of("Demo Test", suiteResult), new XmlSuite());

        Path outputDir = Files.createTempDirectory("custom-artifacts-report");
        try (MockedStatic<Reporter> reporter = Mockito.mockStatic(Reporter.class)) {
            reporter.when(() -> Reporter.getOutput(failedResult)).thenReturn(List.of(
                    "2026-03-07 22:10:10.000: ERROR: Something bad happened</br>",
                    "<br><a href='screenshots/failure.png' target='_blank'> CLICK TO SEE SCREENSHOT </a></br>"
            ));
            reporter.when(() -> Reporter.getOutput(passedResult)).thenReturn(List.of(
                    "2026-03-07 22:10:20.000: INFO: Business step</br>",
                    "2026-03-07 22:10:20.100: DEBUG: Internal details</br>"
            ));
            new CustomArtifactsReporter().generateReport(Collections.emptyList(), List.of(suite), outputDir.toString());
        }

        Path reportFile = outputDir.resolve("custom-artifacts.html");
        iAssert.isTrue(Files.exists(reportFile), "custom-artifacts.html should be generated");

        String html = Files.readString(reportFile);
        iAssert.contains(html, "Demo Suite", "report contains suite name");
        iAssert.contains(html, "tests.SmokeTest", "report contains class name");
        iAssert.contains(html, "openPageTest", "report contains failed method name");
        iAssert.contains(html, "FAILED", "report contains failed status");
        iAssert.contains(html, "href='screenshots/failure.png'", "report contains screenshot href");
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

    private ISuite suite(String name, Map<String, org.testng.ISuiteResult> results, XmlSuite xmlSuite) {
        return (ISuite) Proxy.newProxyInstance(
                ISuite.class.getClassLoader(),
                new Class<?>[]{ISuite.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "getResults" -> results;
                    case "getXmlSuite" -> xmlSuite;
                    case "getAttribute" -> null;
                    case "getAttributeNames" -> Collections.emptySet();
                    case "removeAttribute", "setAttribute", "run", "addListener", "setParentInjector" -> null;
                    case "getMethodsByGroups" -> Collections.emptyMap();
                    case "getAllInvokedMethods", "getExcludedMethods", "getAllMethods" -> Collections.emptyList();
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "ISuite[" + name + "]";
                    default -> null;
                }
        );
    }
}
