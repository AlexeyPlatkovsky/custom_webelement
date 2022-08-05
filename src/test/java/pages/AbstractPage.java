package pages;

import core.web.iPageFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

public abstract class AbstractPage {
  protected WebDriver driver;

  public AbstractPage(WebDriver driver) {
    this.driver = driver;
    this.driver.manage().window().setSize(new Dimension(1920, 1080));
    iPageFactory.initElements(driver, this);
  }
}
