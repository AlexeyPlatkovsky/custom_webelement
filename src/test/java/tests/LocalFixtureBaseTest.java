package tests;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import tests.support.LocalTestPageServer;
import utils.properties.SystemProperties;

public abstract class LocalFixtureBaseTest extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void configureLocalFixtureBaseUrl() {
        SystemProperties.ROOT_URL = LocalTestPageServer.ensureStarted();
        SystemProperties.OS = "unix";
        SystemProperties.SCREEN_MAXIMIZE = false;
    }

    @AfterSuite(alwaysRun = true)
    public void stopLocalFixtureServer() {
        LocalTestPageServer.stop();
    }
}
