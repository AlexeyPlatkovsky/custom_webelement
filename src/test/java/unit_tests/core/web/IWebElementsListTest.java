package unit_tests.core.web;

import core.web.iWebElement;
import core.web.iWebElementsList;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import utils.assertions.iAssert;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"unit"})
public class IWebElementsListTest {

    interface WebDriverJs extends WebDriver, JavascriptExecutor { }

    @Test
    public void listItemReresolvesAfterListDomRefresh() {
        WebDriverJs driver = mock(WebDriverJs.class);
        By locator = By.cssSelector(".result-item");
        iWebElementsList list = new iWebElementsList(driver, "resultItems");
        WebElement initialElement = mock(WebElement.class);
        WebElement rerenderedElement = mock(WebElement.class);
        AtomicBoolean useRerenderedDom = new AtomicBoolean(false);

        list.setLocator(locator);
        when(driver.findElements(locator)).thenAnswer(invocation ->
                useRerenderedDom.get() ? List.of(rerenderedElement) : List.of(initialElement));
        when(driver.executeScript(anyString(), eq(initialElement))).thenReturn(null);
        when(driver.executeScript(anyString(), eq(rerenderedElement))).thenReturn(null);
        when(initialElement.getText()).thenThrow(new StaleElementReferenceException("stale item"));
        when(rerenderedElement.getText()).thenReturn("Reactive alpha entry");
        when(rerenderedElement.getAttribute("value")).thenReturn("");

        iWebElement item = list.get(0);
        useRerenderedDom.set(true);

        iAssert.equalsTo(
                item.getText(),
                "Reactive alpha entry",
                "list item should resolve the current DOM node after the list is re-rendered"
        );
    }

    @Test
    public void xpathListItemsUseIndexedLocatorForItemScopedActions() {
        WebDriverJs driver = mock(WebDriverJs.class);
        By locator = By.xpath("//li[@class='result-item']");
        iWebElementsList list = new iWebElementsList(driver, "resultItems");

        list.setLocator(locator);
        when(driver.findElements(locator)).thenReturn(List.of(mock(WebElement.class), mock(WebElement.class)));

        iWebElement item = list.get(1);

        iAssert.equalsTo(
                item.getLocator().toString(),
                "By.xpath: (//li[@class='result-item'])[2]",
                "xpath-based list items should keep an item-specific locator"
        );
    }
}
