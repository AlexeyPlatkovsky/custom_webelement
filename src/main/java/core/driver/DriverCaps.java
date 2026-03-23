package core.driver;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.io.IOException;
import java.nio.file.Files;

public class DriverCaps {

    public static MutableCapabilities getCaps(DriverNames driverName) {
        MutableCapabilities capabilities = switch (driverName) {
            case CHROME  -> getChromeCaps();
            case FIREFOX -> getFirefoxCaps();
            case LAMBDA  -> getLambdaCaps();
        };
        if (driverName == DriverNames.LAMBDA) {
            iLogger.info("Driver options are : [Lambda caps - credentials redacted]");
        } else {
            iLogger.info("Driver options are : {}", capabilities.toString());
        }
        return capabilities;
    }

    static MutableCapabilities getChromeCaps() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=" + SystemProperties.SCREEN_RESOLUTION)
                .addArguments("--lang=ru-RU")
                .addArguments("--ignore-certificate-errors")
                .addArguments("--disable-extensions")
                .addArguments("--no-sandbox")
                .addArguments("--disable-dev-shm-usage")
                .addArguments("--remote-debugging-port=0")
                .addArguments("--user-data-dir=" + createChromeProfileDir())
                .addArguments("--remote-allow-origins=*");

        if (SystemProperties.OS.equalsIgnoreCase("unix")) {
            options.addArguments("--headless=new")
                    .addArguments("--disable-gpu");
        }
        return options;
    }

    private static String createChromeProfileDir() {
        try {
            return Files.createTempDirectory("chrome-profile-").toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary Chrome profile directory", e);
        }
    }

    static MutableCapabilities getFirefoxCaps() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--window-size=" + SystemProperties.SCREEN_RESOLUTION)
                .addArguments("--lang=ru-RU")
                .addArguments("--remote-allow-origins=*");
        return options;
    }

    static MutableCapabilities getLambdaCaps() {
        MutableCapabilities remoteCapabilities = new MutableCapabilities();
        remoteCapabilities.setCapability("browser", SystemProperties.REMOTE_BROWSER);
        remoteCapabilities.setCapability("version", SystemProperties.BROWSER_VERSION);
        remoteCapabilities.setCapability("platform", SystemProperties.PLATFORM);
        remoteCapabilities.setCapability("resolution", SystemProperties.SCREEN_RESOLUTION);
        remoteCapabilities.setCapability("user", SystemProperties.REMOTE_USERNAME);
        remoteCapabilities.setCapability("accessKey", SystemProperties.REMOTE_KEY);
        remoteCapabilities.setCapability("build", SystemProperties.BUILD_NUMBER);
        remoteCapabilities.setCapability("timezone", "UTC+00:00");
        remoteCapabilities.setCapability("--lang=", SystemProperties.LOCALE);

        if (!RemoteEnvProperties.USE_LOCAL_PORT.isEmpty()) {
            remoteCapabilities.setCapability("tunnel", true);
        }

        return remoteCapabilities;
    }
}
