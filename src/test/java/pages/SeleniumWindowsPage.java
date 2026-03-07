package pages;

import core.web.annotations.PageURL;

@PageURL(value = "/webdriver/interactions/windows/")
public class SeleniumWindowsPage extends SeleniumWebDriverPage {
    private static final String EXPECTED_PATH = "/documentation/webdriver/interactions/windows";

    public boolean isExpectedPathOpened() {
        return driver.getCurrentUrl().contains(EXPECTED_PATH);
    }
}
