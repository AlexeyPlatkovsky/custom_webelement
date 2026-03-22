package unit_tests.core.web;

import core.driver.DriverFactory;
import core.web.iPage;
import core.web.iPageFactory;
import core.web.iWebElement;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;
import utils.assertions.iAssert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"unit"})
public class PageComponentScopeTest {

    interface WebDriverJs extends WebDriver, JavascriptExecutor { }

    @FindBy(css = ".class-component")
    public static class ClassScopedComponent extends iPage {
        @FindBy(css = ".component-value")
        private iWebElement value;

        public String readValue() {
            return value.getText();
        }
    }

    public static class FieldScopedComponent extends iPage {
        @FindBy(css = ".component-value")
        private iWebElement value;

        public String readValue() {
            return value.getText();
        }
    }

    @FindBy(css = ".class-ignored-component")
    public static class FieldOverridesClassScopedComponent extends iPage {
        @FindBy(css = ".component-value")
        private iWebElement value;

        public String readValue() {
            return value.getText();
        }
    }

    public static class PageFixture {
        public ClassScopedComponent classScopedComponent;

        @FindBy(css = ".field-component")
        public FieldScopedComponent fieldScopedComponent;

        @FindBy(css = ".field-override-component")
        public FieldOverridesClassScopedComponent fieldOverridesClassScopedComponent;
    }

    @Test
    public void classLevelScopeResolvesChildLookupWithinComponentRoot() {
        WebDriverJs driver = mock(WebDriverJs.class);
        PageFixture fixture = initFixture(driver);

        configureScopedComponent(driver, ".class-component", "class-root-value");
        configureGlobalFallback(driver, "global-value");

        iAssert.equalsTo(
                fixture.classScopedComponent.readValue(),
                "class-root-value",
                "class-level @FindBy should scope child lookup through the component root"
        );

        verify(driver, never()).findElement(By.cssSelector(".component-value"));
    }

    @Test
    public void fieldLevelScopeResolvesChildLookupWithinComponentRoot() {
        WebDriverJs driver = mock(WebDriverJs.class);
        PageFixture fixture = initFixture(driver);

        configureScopedComponent(driver, ".field-component", "field-root-value");
        configureGlobalFallback(driver, "global-value");

        iAssert.equalsTo(
                fixture.fieldScopedComponent.readValue(),
                "field-root-value",
                "field-level @FindBy should scope child lookup through the component root"
        );

        verify(driver, never()).findElement(By.cssSelector(".component-value"));
    }

    @Test
    public void fieldLevelScopeOverridesClassLevelScopeForComponentRoot() {
        WebDriverJs driver = mock(WebDriverJs.class);
        PageFixture fixture = initFixture(driver);

        configureScopedComponent(driver, ".field-override-component", "field-override-value");
        configureScopedComponent(driver, ".class-ignored-component", "wrong-class-value");
        configureGlobalFallback(driver, "global-value");

        iAssert.equalsTo(
                fixture.fieldOverridesClassScopedComponent.readValue(),
                "field-override-value",
                "field-level @FindBy should override the component class locator"
        );

        verify(driver, never()).findElement(By.cssSelector(".component-value"));
    }

    private PageFixture initFixture(WebDriverJs driver) {
        PageFixture fixture = new PageFixture();

        try (MockedStatic<DriverFactory> driverFactory = Mockito.mockStatic(DriverFactory.class)) {
            driverFactory.when(DriverFactory::getCurrentDriver).thenReturn(driver);
            iPageFactory.initElements(driver, fixture);
        }

        return fixture;
    }

    private void configureScopedComponent(WebDriver driver, String rootCss, String childValue) {
        WebElement rootElement = mock(WebElement.class);
        WebElement childElement = mock(WebElement.class);

        when(driver.findElement(By.cssSelector(rootCss))).thenReturn(rootElement);
        when(rootElement.findElement(By.cssSelector(".component-value"))).thenReturn(childElement);
        when(childElement.getText()).thenReturn(childValue);
        when(childElement.getAttribute("value")).thenReturn(childValue);
    }

    private void configureGlobalFallback(WebDriver driver, String childValue) {
        WebElement globalElement = mock(WebElement.class);

        when(driver.findElement(By.cssSelector(".component-value"))).thenReturn(globalElement);
        when(globalElement.getText()).thenReturn(childValue);
        when(globalElement.getAttribute("value")).thenReturn(childValue);
    }
}
