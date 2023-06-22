package core.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.SkipException;
import utils.logging.iLogger;

import java.util.*;
import java.util.stream.Collectors;

public class iWebElementsList extends iWebElement implements List<iWebElement> {

    public iWebElementsList(WebDriver driver, String name) {
        super(driver, name);
    }

    public int size() {
        int size = getAll().size();
        iLogger.info("Size of list {} = {}", name, String.valueOf(size));
        return size;
    }

    @Override
    public boolean isEmpty() {
        return getAll().size() == 0;
    }

    @Override
    public boolean add(iWebElement webElement) {
        return true;
    }

    @Override
    public void add(int index, iWebElement element) {
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public iWebElement remove(int index) {
        return null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends iWebElement> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends iWebElement> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public iWebElement set(int index, iWebElement element) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public iWebElementsList template(String text) {
        setupLocator(text);
        return this;
    }

    public List<String> getTexts() {
        return getTexts(false);
    }

    public List<String> getTexts(boolean allElementShouldBeVisible) {
        List<iWebElement> allElements = getAll();
        if (allElementShouldBeVisible)
            allElements.forEach(iWebElement::waitToBeVisible);
        return allElements.stream().map(iWebElement::getText).collect(Collectors.toList());
    }

    public boolean contains(Object o) {
        return getAll().contains(o);
    }

    public Iterator<iWebElement> iterator() {
        return getAll().iterator();
    }

    public Object[] toArray() {
        return getAll().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return getAll().toArray(a);
    }

    public iWebElement get(int index) {
        try {
            return getAll().get(index);
        } catch (IndexOutOfBoundsException e) {
            iLogger.error("Can't find elements for locator {}", getLocator().toString());
        }
        return null;
    }

    public ListIterator<iWebElement> listIterator() {
        return getAll().listIterator();
    }

    public ListIterator<iWebElement> listIterator(int index) {
        return getAll().listIterator(index);
    }

    public List<iWebElement> subList(int fromIndex, int toIndex) {
        return getAll().subList(fromIndex, toIndex);
    }

    public boolean isDisplayed() {
        return getWebElements().get(0).isDisplayed();
    }

    public List<iWebElement> getAll() {
        return getWebElements();
    }

    private List<iWebElement> getWebElements() {
        List<iWebElement> elElements = new ArrayList<>();
        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(getLocator(), 0));
        } catch (Exception e) {
            iLogger.info("No webelements with locator {}", getLocator().toString());
        }

        for (int i = 0; i < getDriver().findElements(getLocator()).size(); i++) {
            elElements.add(new iWebElement(driver, name, getLocatorWithId(getLocator(), i + 1)));
        }
        return elElements;
    }

    private By getLocatorWithId(By byLocator, int index) {
        String locator = getLocator().toString().replaceAll("(By\\.)(\\w+)(: )", "").trim();
        switch (byLocator.getClass().getSimpleName()) {
            case "ByXPath" -> {
                locator = "(" + locator + ")[" + index + "]";
                return new By.ByXPath(locator);
            }
            case "ByCssSelector" -> {
                locator = locator + ":nth-child(" + index + ")";
                return new By.ByCssSelector(locator);
            }
            default -> {
                iLogger.error("iWebElementList works only with CSS and XPATH selectors");
                throw new IllegalArgumentException("iWebElementList works only with CSS and XPATH selectors");
            }
        }
    }

    public iWebElement getChildWithText(String expectedText) {
        for (iWebElement element : getWebElements()) {
            if (element.getChild(By.xpath(String.format("//*[text()='%s']", expectedText))).isDisplayed()) {
                return element;
            }
        }
        throw new SkipException("No child with expected text");
    }

    public void clickAll() {
        getAll().forEach(iWebElement::click);
    }

    public List<String> getTextForVisibleElements() {
        List<iWebElement> allElements = getAll();
        return allElements.stream().filter(iWebElement::isDisplayed).map(iWebElement::getText).collect(Collectors.toList());
    }
}
