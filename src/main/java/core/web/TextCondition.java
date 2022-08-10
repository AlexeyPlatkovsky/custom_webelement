package core.web;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import utils.logging.iLogger;

public class TextCondition {

  public static ExpectedCondition<Boolean> textIsNotEmpty(final iWebElement element) {
    return input -> {
      try {
        iLogger.info("Wait until text is not empty");
        String elementText = element.getText();
        return !elementText.isEmpty();
      } catch (StaleElementReferenceException e) {
        iLogger.error("Can't get text for element {}", element.toString());
        return null;
      }
    };
  }
}
