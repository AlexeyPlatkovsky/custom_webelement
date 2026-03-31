package unit_tests.core.web;

import core.driver.DriverFactory;
import core.web.annotations.PageURL;
import core.web.iPage;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import utils.assertions.iAssert;
import utils.properties.SystemProperties;

import static org.mockito.Mockito.verify;

@Test(groups = {"unit"})
public class IPageOpenStateTest {

    interface WebDriverJs extends WebDriver, JavascriptExecutor {
    }

    @PageURL("https://example.com")
    private abstract static class ExampleBasePage extends iPage {
    }

    @PageURL("/practice/")
    private static class PracticePage extends ExampleBasePage {
    }

    @PageURL("/")
    private static class HomePage extends ExampleBasePage {
    }

    @Test
    public void defaultIsOpenedReturnsTrueWhenCurrentUrlMatchesResolvedPageUrl() {
        WebDriverJs driver = Mockito.mock(WebDriverJs.class);
        Mockito.when(driver.getCurrentUrl()).thenReturn("https://example.com/practice/");

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);

            iAssert.isTrue(new PracticePage().isOpened(),
                    "default isOpened should use the resolved @PageURL hierarchy");
        }
    }

    @Test
    public void defaultIsOpenedTreatsTrailingSlashAsEquivalent() {
        WebDriverJs driver = Mockito.mock(WebDriverJs.class);
        Mockito.when(driver.getCurrentUrl()).thenReturn("https://example.com/practice");

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);

            iAssert.isTrue(new PracticePage().isOpened(),
                    "default isOpened should ignore trailing slash differences");
        }
    }

    @Test
    public void defaultIsOpenedReturnsFalseWhenCurrentUrlPointsToDifferentPage() {
        WebDriverJs driver = Mockito.mock(WebDriverJs.class);
        Mockito.when(driver.getCurrentUrl()).thenReturn("https://example.com/practice-test-login/");

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);

            iAssert.isFalse(new PracticePage().isOpened(),
                    "default isOpened should reject a different page path");
        }
    }

    @Test
    public void defaultIsOpenedDoesNotTreatHomePageAsEveryPageOnTheSameDomain() {
        WebDriverJs driver = Mockito.mock(WebDriverJs.class);
        Mockito.when(driver.getCurrentUrl()).thenReturn("https://example.com/practice/");

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);

            iAssert.isFalse(new HomePage().isOpened(),
                    "root page should not be treated as opened on every child page");
        }
    }

    @Test
    public void defaultIsOpenedIgnoresSystemRootUrlWhenHierarchyContainsAbsoluteBasePage() {
        String originalRootUrl = SystemProperties.ROOT_URL;
        WebDriverJs driver = Mockito.mock(WebDriverJs.class);
        Mockito.when(driver.getCurrentUrl()).thenReturn("https://example.com/practice/");

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            SystemProperties.ROOT_URL = "http://localhost:8080";
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);

            iAssert.isTrue(new PracticePage().isOpened(),
                    "absolute @PageURL base page should take precedence over system root url");
        } finally {
            SystemProperties.ROOT_URL = originalRootUrl;
        }
    }

    @Test
    public void openPageUsesAbsoluteBasePageInsteadOfSystemRootUrl() {
        String originalRootUrl = SystemProperties.ROOT_URL;
        WebDriverJs driver = Mockito.mock(WebDriverJs.class);

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            SystemProperties.ROOT_URL = "http://localhost:8080";
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);

            new PracticePage().openPage();

            verify(driver).get("https://example.com/practice");
        } finally {
            SystemProperties.ROOT_URL = originalRootUrl;
        }
    }
}
