package pages;

import core.web.iPage;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

@FindBy(css = "#class-ignored-component")
public class FieldOverridesClassDemoComponent extends iPage {
    @FindBy(css = ".component-input")
    private iWebElement componentInput;

    @FindBy(css = ".component-value")
    private iWebElement componentValue;

    public void enterText(String text) {
        iLogger.info("Enter field-overrides-class component text '{}'", text);
        componentInput.clear();
        componentInput.sendKeys(text);
    }

    public String getValue() {
        return componentValue.getText();
    }
}
