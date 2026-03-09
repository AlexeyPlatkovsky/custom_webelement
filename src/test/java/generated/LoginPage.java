package generated;

import core.annotations.CacheElement;
import core.annotations.PageURL;
import core.annotations.Waiter;
import core.web.iWebElement;
import org.openqa.selenium.support.FindBy;
import pages.AbstractPage;
import utils.iPageFactory;
import utils.logging.iLogger;

@PageURL("https://myapp.example.com/login")
public class LoginPage extends AbstractPage {

    @CacheElement
    @FindBy(tagName = "h2")
    private iWebElement pageHeading;

    @FindBy(id = "username")
    private iWebElement usernameInput;

    @FindBy(id = "password")
    private iWebElement passwordInput;

    @FindBy(id = "remember-me")
    private iWebElement rememberMeCheckbox;

    @FindBy(id = "login-button")
    private iWebElement loginButton;

    @Waiter(waitFor = 3)
    @FindBy(id = "flash-message")
    private iWebElement flashMessage;

    @CacheElement
    @FindBy(id = "forgot-password-link")
    private iWebElement forgotPasswordLink;

    @CacheElement
    @FindBy(id = "register-link")
    private iWebElement registerLink;

    public LoginPage() {
        iPageFactory.initElements(this.driver, this);
    }

    public LoginPage enterUsername(String username) {
        iLogger.info("Entering username: " + username);
        usernameInput.clear();
        usernameInput.sendKeys(username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        iLogger.info("Entering password into password field");
        passwordInput.clear();
        passwordInput.sendKeys(password);
        return this;
    }

    public LoginPage checkRememberMe() {
        iLogger.info("Checking 'Remember me' checkbox");
        if (!rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
        return this;
    }

    public LoginPage uncheckRememberMe() {
        iLogger.info("Unchecking 'Remember me' checkbox");
        if (rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
        return this;
    }

    public void clickLoginButton() {
        iLogger.info("Clicking the Login button to submit credentials");
        loginButton.click();
    }

    public LoginPage loginWithCredentials(String username, String password) {
        iLogger.info("Logging in with username: " + username);
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        return this;
    }

    public ForgotPasswordPage clickForgotPasswordLink() {
        iLogger.info("Clicking 'Forgot your password?' link");
        forgotPasswordLink.click();
        return new ForgotPasswordPage();
    }

    public RegisterPage clickRegisterLink() {
        iLogger.info("Clicking 'Create an account' link to navigate to registration");
        registerLink.click();
        return new RegisterPage();
    }

    public String getFlashMessageText() {
        iLogger.info("Retrieving flash/error message text");
        return flashMessage.getText();
    }

    public boolean isFlashMessageDisplayed() {
        iLogger.info("Checking if flash message is displayed");
        return flashMessage.isDisplayed();
    }

    public String getPageHeadingText() {
        iLogger.info("Retrieving page heading text");
        return pageHeading.getText();
    }
}