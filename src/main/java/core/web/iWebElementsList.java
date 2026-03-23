package core.web;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import utils.logging.iLogger;
import utils.properties.WebElementProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
        return getAll().isEmpty();
    }

    @Override
    public boolean add(iWebElement webElement) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public void add(int index, iWebElement element) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public iWebElement remove(int index) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends iWebElement> c) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends iWebElement> c) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public iWebElement set(int index, iWebElement element) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("iWebElementsList is read-only");
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

    public @NotNull Iterator<iWebElement> iterator() {
        return getAll().iterator();
    }

    public Object @NotNull [] toArray() {
        return getAll().toArray();
    }

    public <T> T @NotNull [] toArray(T @NotNull [] a) {
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

    public @NotNull ListIterator<iWebElement> listIterator() {
        return getAll().listIterator();
    }

    public @NotNull ListIterator<iWebElement> listIterator(int index) {
        return getAll().listIterator(index);
    }

    public @NotNull List<iWebElement> subList(int fromIndex, int toIndex) {
        return getAll().subList(fromIndex, toIndex);
    }

    public boolean isDisplayed() {
        return getWebElements().getFirst().isDisplayed();
    }

    public List<iWebElement> getAll() {
        return getWebElements();
    }

    private List<iWebElement> getWebElements() {
        List<iWebElement> elElements = new ArrayList<>();
        try {
            wait.until(driver -> !findElementsInScope().isEmpty() ? Boolean.TRUE : null);
        } catch (Exception e) {
            iLogger.info("No webelements with locator {}", getLocator().toString());
        }

        int size = findElementsInScope().size();
        for (int index = 0; index < size; index++) {
            elElements.add(new IndexedListItem(index));
        }
        return elElements;
    }

    public iWebElement getChildWithText(String expectedText) {
        for (iWebElement element : getWebElements()) {
            if (!element.findElements(By.xpath(".//*[text()='" + expectedText + "']")).isEmpty()) {
                return element;
            }
        }
        throw new SkipException("No child with expected text: " + expectedText);
    }

    public void clickAll() {
        getAll().forEach(iWebElement::click);
    }

    public List<String> getTextForVisibleElements() {
        List<iWebElement> allElements = getAll();
        return allElements.stream().filter(iWebElement::isDisplayed).map(iWebElement::getText).collect(Collectors.toList());
    }

    private By getItemLocator(int index) {
        if (getLocator() instanceof By.ByXPath) {
            return By.xpath("(" + getLocatorValue() + ")[" + (index + 1) + "]");
        }
        return getLocator();
    }

    private class IndexedListItem extends iWebElement {
        private final int index;
        private final By collectionLocator;

        private IndexedListItem(int index) {
            super(iWebElementsList.this.driver, iWebElementsList.this.name, iWebElementsList.this.getItemLocator(index));
            this.index = index;
            this.collectionLocator = iWebElementsList.this.getLocator();
            this.wait = iWebElementsList.this.wait;
            setParent(iWebElementsList.this.getParentElement());
        }

        @Override
        public WebElement getWebElement(boolean isHighlight) {
            try {
                WebElement element = wait.until(webDriver -> {
                    List<WebElement> currentElements = iWebElementsList.this.findElementsInScope();
                    return index < currentElements.size() ? currentElements.get(index) : null;
                });
                if (isHighlight) {
                    highlightElement(element);
                }
                return element;
            } catch (TimeoutException e) {
                iLogger.error(String.format("Timed out waiting for list item %s[%s] with locator %s",
                        name, index, collectionLocator));
                throw new SkipException("Don't see " + this);
            }
        }

        private void highlightElement(WebElement element) {
            boolean shouldBeHighlighted = Boolean.parseBoolean(WebElementProperties.WEBELEMENT_BORDER_SHOULD_BE_HIGHLIGHTED);
            if (shouldBeHighlighted) {
                String border = String.format("arguments[0].style.border='%s solid %s'",
                        WebElementProperties.WEBELEMENT_BORDER_WIDTH, WebElementProperties.WEBELEMENT_BORDER_COLOR);
                try {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(border, element);
                } catch (Exception ignored) {
                }
            }
        }
    }
}

