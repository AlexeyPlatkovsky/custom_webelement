package unit_tests.utils.logging;

import core.driver.DriverFactory;
import org.mockito.Mockito;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import utils.assertions.iAssert;
import utils.logging.iLogger;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

@Test(groups = {"unit"}, singleThreaded = true)
public class ILoggerScreenshotTest {

    @AfterMethod(alwaysRun = true)
    public void cleanupDriver() throws Exception {
        setDriver(null);
    }

    @Test
    public void takeScreenshotShouldReturnNullWhenNoActiveDriver() {
        iLogger.ScreenshotArtifact screenshotArtifact = iLogger.takeScreenshot();
        iAssert.isNull(screenshotArtifact, "screenshot artifact is null when there is no active driver");
    }

    @Test
    public void takeScreenshotShouldPersistFileAndReturnRelativePath() throws Exception {
        byte[] bytes = new byte[]{1, 2, 3, 4, 5};
        TakesScreenshot mockDriver = Mockito.mock(TakesScreenshot.class, Mockito.withSettings().extraInterfaces(WebDriver.class));
        Mockito.when(mockDriver.getScreenshotAs(OutputType.BYTES)).thenReturn(bytes);
        setDriver((WebDriver) mockDriver);

        String initialUserDir = System.getProperty("user.dir");
        Path testUserDir = Files.createTempDirectory("ilogger-screenshot");
        System.setProperty("user.dir", testUserDir.toString());

        try {
            iLogger.ScreenshotArtifact screenshotArtifact = iLogger.takeScreenshot();
            iAssert.isNotNull(screenshotArtifact, "screenshot artifact is created");
            iAssert.isTrue(Files.exists(screenshotArtifact.filePath()), "screenshot file is persisted on disk");
            iAssert.isTrue(screenshotArtifact.reportRelativePath().startsWith("screenshots/"), "report path uses screenshots folder");
            iAssert.isTrue(screenshotArtifact.fileName().endsWith(".png"), "screenshot file has png extension");
        } finally {
            System.setProperty("user.dir", initialUserDir);
        }
    }

    @SuppressWarnings("unchecked")
    private void setDriver(WebDriver driver) throws Exception {
        Field driverField = DriverFactory.class.getDeclaredField("DRIVER");
        driverField.setAccessible(true);
        ThreadLocal<WebDriver> threadLocalDriver = (ThreadLocal<WebDriver>) driverField.get(null);
        if (driver == null) {
            threadLocalDriver.remove();
        } else {
            threadLocalDriver.set(driver);
        }
    }
}
