package unit_tests.core.web;

import core.driver.DriverFactory;
import core.web.iPage;
import core.web.iPageFactory;
import core.web.iWebElement;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test(groups = "unit")
public class PageComponentInitTest {

    // iWebElement constructor casts driver to JavascriptExecutor, so the mock must implement both
    interface WebDriverJs extends WebDriver, JavascriptExecutor { }

    @FindBy(css = ".sample-component")
    public static class SampleComponent extends iPage {
        @FindBy(css = ".sample-btn")
        public iWebElement sampleButton;
    }

    public static class PageFixture {
        public SampleComponent sampleComponent;
    }

    @Test
    public void componentFieldIsInitializedByPageFactory() {
        WebDriverJs mockDriver = mock(WebDriverJs.class);
        PageFixture fixture = new PageFixture();

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(mockDriver);
            iPageFactory.initElements(mockDriver, fixture);
        }

        assertNotNull(fixture.sampleComponent, "component field should be initialized by iPageFactory");
    }

    @Test
    public void componentElementsAreInitializedByPageFactory() {
        WebDriverJs mockDriver = mock(WebDriverJs.class);
        PageFixture fixture = new PageFixture();

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(mockDriver);
            iPageFactory.initElements(mockDriver, fixture);
        }

        assertNotNull(fixture.sampleComponent.sampleButton,
                "component's @FindBy element field should be initialized");
        assertEquals(fixture.sampleComponent.sampleButton.getDriver(), mockDriver,
                "component elements should use the driver passed to iPageFactory");
    }

    @Test
    public void componentElementLocatorIsSetCorrectly() {
        WebDriverJs mockDriver = mock(WebDriverJs.class);
        PageFixture fixture = new PageFixture();

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(mockDriver);
            iPageFactory.initElements(mockDriver, fixture);
        }

        iWebElement button = fixture.sampleComponent.sampleButton;
        assertNotNull(button.getLocator(), "element locator should be set from @FindBy annotation");
        assertEquals(button.getLocator().toString(), "By.cssSelector: .sample-btn",
                "locator should match the @FindBy css value");
    }

    @Test
    public void preInitializedComponentIsNotReplacedByPageFactory() {
        WebDriverJs initialDriver = mock(WebDriverJs.class);
        WebDriverJs mockDriver = mock(WebDriverJs.class);
        PageFixture fixture = new PageFixture();

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(initialDriver);
            SampleComponent preCreated = new SampleComponent();
            fixture.sampleComponent = preCreated;
            iPageFactory.initElements(mockDriver, fixture);

            assertEquals(fixture.sampleComponent, preCreated,
                    "pre-initialized component field should not be replaced");
            assertEquals(fixture.sampleComponent.sampleButton.getDriver(), mockDriver,
                    "pre-initialized component elements should be rebound to the supplied driver");
        }
    }
}
