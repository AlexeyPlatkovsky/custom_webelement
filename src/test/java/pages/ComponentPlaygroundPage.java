package pages;

import core.web.annotations.CacheElement;
import core.web.annotations.PageURL;
import core.web.iPage;
import core.web.iWebElement;
import core.web.iWebElementsList;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

import java.util.List;

@PageURL("/component-playground.html")
public class ComponentPlaygroundPage extends iPage {
    @FindBy(id = "global-input")
    private iWebElement globalInput;

    @FindBy(id = "global-input")
    @CacheElement
    private iWebElement cachedGlobalInput;

    @FindBy(css = ".result-item")
    private iWebElementsList resultItems;

    private ClassScopedDemoComponent classScopedComponent;

    @FindBy(css = "#field-component")
    private FieldScopedDemoComponent fieldScopedComponent;

    @FindBy(css = "#field-override-component")
    private FieldOverridesClassDemoComponent fieldOverridesClassComponent;

    public void enterGlobalText(String text) {
        iLogger.info("Enter global page text '{}'", text);
        globalInput.clear();
        globalInput.sendKeys(text);
    }

    public String getGlobalText() {
        return globalInput.getText();
    }

    public long getCachedElementFindTime() {
        return getElementFindTime(cachedGlobalInput);
    }

    public long getNonCachedElementFindTime() {
        return getElementFindTime(globalInput);
    }

    public List<String> getAllResultItems() {
        return resultItems.getTextForVisibleElements();
    }

    public void enterClassScopedText(String text) {
        classScopedComponent.enterText(text);
    }

    public String getClassScopedValue() {
        return classScopedComponent.getValue();
    }

    public void enterFieldScopedText(String text) {
        fieldScopedComponent.enterText(text);
    }

    public String getFieldScopedValue() {
        return fieldScopedComponent.getValue();
    }

    public void enterFieldOverrideText(String text) {
        fieldOverridesClassComponent.enterText(text);
    }

    public String getFieldOverrideValue() {
        return fieldOverridesClassComponent.getValue();
    }

    private long getElementFindTime(iWebElement element) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            element.getText();
        }
        long executionTime = System.currentTimeMillis() - startTime;
        iLogger.info("Find time for element " + element + " = " + executionTime);
        return executionTime;
    }
}
