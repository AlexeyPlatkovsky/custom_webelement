package core.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.logging.iLogger;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static core.tools.ReflectionUtils.*;

public class iPageFactory {

  public static void initElements(WebDriver driver, Object section) {
    List<Field> listOfFields = new ArrayList<>();
    Class<?> type = section.getClass();
    while (type != null) {
      listOfFields.addAll(Arrays.asList(type.getDeclaredFields()));
      type = type.getSuperclass();
    }
    for (Field field : listOfFields) {
      initElement(driver, field, section);
    }
  }

  private static void initElement(WebDriver driver, Field field, Object section) {
    try {
      if (!hasAnnotation(field)) {
        return;
      }
      Object instance = getValueField(field, section);
      if (instance == null) {
        instance = newInstance(driver, field);
      }
      if (instance != null) {
        Class<?> type = instance.getClass();
        if (isClass(type, iWebElement.class) || isClass(type, iWebElementsList.class)) {
          iWebElement el = (iWebElement) instance;
          By locator = getLocatorFromField(field);
          Waiter waiter = getWaiterFromField(field);
          if (locator != null) {
            el.setLocator(locator);
          }
          if (waiter != null) {
            el.setWaiter(waiter.waitFor());
          }
        }
        field.set(section, instance);
      }
    } catch (IllegalAccessException e) {
      iLogger.error(e);
    }
  }

  private static Object newInstance(WebDriver driver, Field field) {
    if (isClass(field, iWebElementsList.class) || isList(field)) {
      return new iWebElementsList(driver, field.getName());
    }
    if (isInterface(field, WebElement.class) || isInterface(field, iWebElement.class)) {
      return new iWebElement(driver, field.getName());
    } else {
      return null;
    }
  }

  private static boolean isList(Field field) {
    return isInterface(field, List.class)
            && isInterface(getGenericType(field), WebElement.class);
  }

  private static Class<?> getGenericType(Field field) {
    try {
      return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    } catch (Exception e) {
      iLogger.error(e);
    }
    return null;
  }

  private static By getLocatorFromField(Field field) {
    if (hasAnnotation(field)) {
      return findByToBy(field.getAnnotation(FindBy.class));
    }
    return null;
  }

  private static Waiter getWaiterFromField(Field field) {
    if (hasAnnotation(field)) {
      return field.getAnnotation(Waiter.class);
    }
    return null;
  }

  private static boolean hasAnnotation(Field field) {
    return field.isAnnotationPresent(FindBy.class)
            || field.getType().isAnnotationPresent(FindBy.class);
  }

  private static By findByToBy(FindBy locator) {
    if (locator == null) {
      return null;
    }
    if (!locator.id().isEmpty()) {
      return By.id(locator.id());
    }
    if (!locator.className().isEmpty()) {
      return By.className(locator.className());
    }
    if (!locator.xpath().isEmpty()) {
      return By.xpath(locator.xpath());
    }
    if (!locator.css().isEmpty()) {
      return By.cssSelector(locator.css());
    }
    if (!locator.linkText().isEmpty()) {
      return By.linkText(locator.linkText());
    }
    if (!locator.name().isEmpty()) {
      return By.name(locator.name());
    }
    if (!locator.partialLinkText().isEmpty()) {
      return By.partialLinkText(locator.partialLinkText());
    }
    if (!locator.tagName().isEmpty()) {
      return By.tagName(locator.tagName());
    }
    return null;
  }
}
