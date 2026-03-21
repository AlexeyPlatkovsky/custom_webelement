package tests;

import core.driver.DriverFactory;
import core.driver.DriverNames;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import utils.logging.TestListener;
import utils.logging.iLogger;
import utils.readers.PropertyReader;

@Listeners(TestListener.class)
public class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void setupTestClass() {
        PropertyReader.readProperties();
        iLogger.setConsoleLogOnlyInfo(true);
        iLogger.setLogOnlyInfo(true);
    }

    @AfterMethod(alwaysRun = true)
    protected void tearDown(ITestResult result) {
        iLogger.info("Close browser");
        DriverFactory.disposeCurrentDriver();
    }
}
