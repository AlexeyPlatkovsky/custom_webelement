package ai.generator;

import ai.crawler.PageSnapshot;
import ai.provider.AiRequest;

/**
 * Builds the system prompt and user message for Page Object generation requests.
 * A single instance is stateless and may be reused across multiple {@link PageSnapshot}s.
 */
public class PromptBuilder {

    // Public so unit tests in a different package can assert on the prompt content
    public static final String SYSTEM_PROMPT =
        "You are an expert Java Selenium 4 test automation engineer.\n"
        + "Generate a Page Object class from the provided page snapshot.\n"
        + "\n"
        + "MANDATORY CONVENTIONS — follow exactly, no exceptions:\n"
        + "\n"
        + "1.  Package declaration must be: package generated;\n"
        + "2.  Class must extend AbstractPage (import pages.AbstractPage)\n"
        + "3.  Annotate class with @PageURL(\"<actual url>\") from core.annotations.PageURL\n"
        + "4.  Fields: iWebElement or iWebElementsList ONLY — never raw WebElement\n"
        + "5.  Constructor: call iPageFactory.initElements(this.driver, this); NEVER PageFactory\n"
        + "6.  Locator priority: id > data-testid > aria-label > stable CSS class > XPath\n"
        + "7.  Apply @CacheElement to stable, static elements (headers, labels, nav, fixed buttons)\n"
        + "8.  Apply @Waiter(waitFor = 3) to elements that load slowly or appear after user action\n"
        + "9.  Every field must have @FindBy annotation\n"
        + "10. Action methods: return this when staying on the same page\n"
        + "    Action methods: return new TargetPage() when navigating to a new page\n"
        + "11. Log every user-visible action: iLogger.info(\"Descriptive action description\")\n"
        + "12. No Thread.sleep(), no @Test methods, no assertions inside Page Objects\n"
        + "\n"
        + "FIELD NAMING:\n"
        + "  Inputs  : usernameInput, emailInput, passwordInput, searchField\n"
        + "  Buttons : loginButton, submitButton, cancelButton, searchButton\n"
        + "  Links   : forgotPasswordLink, registerLink, homeLink, backLink\n"
        + "  Messages: errorMessage, successBanner, flashMessage, loadingSpinner\n"
        + "  Lists   : searchResults, menuItems, productCards, tableRows\n"
        + "\n"
        + "IMPORTS — include only those actually used:\n"
        + "  import core.annotations.CacheElement;\n"
        + "  import core.annotations.PageURL;\n"
        + "  import core.annotations.Waiter;\n"
        + "  import core.web.iWebElement;\n"
        + "  import core.web.iWebElementsList;\n"
        + "  import org.openqa.selenium.Keys;\n"
        + "  import org.openqa.selenium.support.FindBy;\n"
        + "  import pages.AbstractPage;\n"
        + "  import utils.iPageFactory;\n"
        + "  import utils.logging.iLogger;\n"
        + "\n"
        + "OUTPUT FORMAT:\n"
        + "  - Raw Java source code ONLY\n"
        + "  - No markdown code fences (no ```java or ```)\n"
        + "  - No explanation text before or after the code\n"
        + "  - The output must be syntactically valid Java 21 that compiles as-is\n";

    /**
     * Builds a complete {@link AiRequest} for generating a Page Object from the given snapshot.
     * If the snapshot contains a screenshot, it is passed as the vision input.
     */
    public AiRequest buildRequest(PageSnapshot snapshot) {
        return new AiRequest(SYSTEM_PROMPT, buildUserMessage(snapshot), snapshot.getScreenshotBase64());
    }

    /**
     * Builds the user-facing message containing the page data.
     * Public so unit tests in a different package can assert on message structure.
     */
    public String buildUserMessage(PageSnapshot snapshot) {
        return "Generate a Page Object for the following page.\n\n"
            + "URL: " + snapshot.getUrl() + "\n"
            + "Title: " + snapshot.getTitle() + "\n\n"
            + "=== CLEANED HTML ===\n"
            + snapshot.getCleanedHtml() + "\n\n"
            + "=== ACCESSIBILITY TREE (JSON) ===\n"
            + snapshot.getAccessibilityTree() + "\n";
    }
}
