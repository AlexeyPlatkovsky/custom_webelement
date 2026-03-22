package pages;

import core.web.iPage;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

@FindBy(css = "#class-component")
public class ClassScopedDemoComponent extends iPage {
    @FindBy(css = ".component-input")
    private iWebElement componentInput;

    @FindBy(css = ".component-value")
    private iWebElement componentValue;

    public void enterText(String text) {
        iLogger.info("Enter class-scoped component text '{}'", text);
        componentInput.clear();
        componentInput.sendKeys(text);
    }

    public String getValue() {
        return componentValue.getText();
    }
}
