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

    @FindBy(css = "#reactive-result-list .reactive-result-item")
    private iWebElementsList reactiveResultItems;

    @FindBy(xpath = "//*[@id='reactive-result-list']//li[contains(@class,'reactive-result-item')]")
    private iWebElementsList xpathReactiveResultItems;

    @FindBy(id = "rerender-results")
    private iWebElement rerenderResultsButton;

    @FindBy(id = "capture-selection")
    private iWebElement captureSelectionButton;

    @FindBy(id = "selection-output")
    private iWebElement selectionOutput;

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

    public String getReactiveItemTextAfterRerender(int index) {
        iWebElement item = reactiveResultItems.get(index);
        rerenderResultsButton.click();
        return item.getText();
    }

    public String captureReactiveItemSelection(int index) {
        xpathReactiveResultItems.get(index).selectTextInElement();
        captureSelectionButton.click();
        return selectionOutput.getText();
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
