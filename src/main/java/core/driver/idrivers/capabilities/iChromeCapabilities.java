package core.driver.idrivers.capabilities;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.properties.SystemProperties;

public class iChromeCapabilities extends MutableCapabilities {
    ChromeOptions options;

    public iChromeCapabilities() {
        options = new ChromeOptions();
        options.addArguments("--window-size=" + SystemProperties.SCREEN_RESOLUTION)
                .addArguments("--lang=ru-RU")
        ;
    }
}
