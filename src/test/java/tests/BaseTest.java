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
    }

    @AfterMethod(alwaysRun = true)
    protected void tearDown(ITestResult result) {
        WebDriver driver = DriverFactory.getCurrentDriver();
        if (result.getStatus() == ITestResult.FAILURE && DriverFactory.driverName().equals(DriverNames.LAMBDA)) {
            ((JavascriptExecutor) driver).executeScript("lambda-status=failed");
            iLogger.info("Close browser");
            driver.quit();
        }
        if (driver != null) {
            driver.quit();
        }
    }
}
