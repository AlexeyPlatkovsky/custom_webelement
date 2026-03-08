package smoke;

import ai.crawler.DomCleaner;
import ai.crawler.PageSnapshot;
import ai.provider.AiProvider;
import ai.provider.AiRequest;
import ai.provider.AiResponse;
import ai.provider.AnthropicProvider;
import ai.provider.AuthConfig;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.logging.iLogger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * End-to-end smoke test: static HTML fixture → DomCleaner → prompt → Anthropic API → printed output.
 * Run with: ./gradlew test -Dsuite=smoke -PskipAllure
 * Requires: ANTHROPIC_API_KEY environment variable set.
 */
@Test(groups = "smoke")
public class PageObjectGenerationSmokeTest {

    @BeforeClass
    public void configureProxyAuth() {
        // Java's HttpClient does not forward proxy credentials from system properties by default.
        // Register a default Authenticator so the outbound proxy challenge is answered correctly.
        String user = System.getProperty("https.proxyUser",
            System.getProperty("http.proxyUser", ""));
        String pass = System.getProperty("https.proxyPassword",
            System.getProperty("http.proxyPassword", ""));
        if (!user.isBlank()) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });
        }
    }

    // Realistic login page HTML (modelled on the-internet.herokuapp.com/login structure)
    private static final String FIXTURE_HTML = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Login Page</title>
          <link rel="stylesheet" href="/styles.css">
          <style>body { font-family: sans-serif; } .error { color: red; }</style>
        </head>
        <body>
          <div class="navbar">
            <a href="/" class="brand">MyApp</a>
            <nav>
              <a href="/about">About</a>
              <a href="/contact">Contact</a>
            </nav>
          </div>
          <div class="container" id="main-content">
            <h2>Login to Your Account</h2>
            <div id="flash-message" class="error" style="display:none;"></div>
            <form id="login-form" action="/authenticate" method="post">
              <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" placeholder="Enter your username"
                       data-testid="username-input" required autofocus>
              </div>
              <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Enter your password"
                       data-testid="password-input" required>
              </div>
              <div class="form-group">
                <label>
                  <input type="checkbox" id="remember-me" name="remember">
                  Remember me
                </label>
              </div>
              <button type="submit" id="login-button" class="btn btn-primary"
                      data-testid="login-submit">Login</button>
            </form>
            <div class="links">
              <a href="/forgot-password" id="forgot-password-link">Forgot your password?</a>
              <a href="/register" id="register-link">Create an account</a>
            </div>
          </div>
          <footer>
            <p>&copy; 2024 MyApp. All rights reserved.</p>
          </footer>
          <script src="/app.js"></script>
          <script>
            document.getElementById('login-form').addEventListener('submit', function(e) {
              console.log('form submitted');
            });
          </script>
        </body>
        </html>
        """;

    private static final String TARGET_URL = "https://myapp.example.com/login";

    private static final String SYSTEM_PROMPT = """
        You are an expert Java Selenium 4 test automation engineer. Your task is to generate a
        Page Object class from the provided page data.

        MANDATORY FRAMEWORK CONVENTIONS — follow these exactly, no exceptions:

        1. Package: `generated`
        2. Extend `AbstractPage` (do not extend anything else)
        3. Use `@PageURL("URL")` annotation on the class
        4. Fields: ONLY `iWebElement` or `iWebElementsList` — NEVER raw `WebElement`
        5. Initialization: `iPageFactory.initElements(this.driver, this)` in constructor — NEVER `PageFactory`
        6. Locator priority: id > data-testid > aria-label > stable CSS class > XPath (last resort)
        7. Apply `@CacheElement` to stable, non-dynamic elements (headers, static nav, labels)
        8. Apply `@Waiter(waitFor = 3)` to elements that may load slowly
        9. Use `@FindBy` for all fields
        10. Action methods return `this` when staying on the same page, or `new TargetPage()` when navigating away
        11. No `Thread.sleep()`, no `@Test` methods, no assertions in Page Objects
        12. Logging: `iLogger.info("Clicking login button")` in action methods

        FIELD NAMING RULES:
        - Inputs: `emailInput`, `usernameInput`, `passwordInput`, `searchField`
        - Buttons: `loginButton`, `submitButton`, `cancelButton`
        - Links: `forgotPasswordLink`, `registerLink`, `homeLink`
        - Messages: `errorMessage`, `successBanner`, `flashMessage`

        IMPORTS — use exactly these when needed:
        ```
        import core.annotations.CacheElement;
        import core.annotations.PageURL;
        import core.annotations.Waiter;
        import core.web.iWebElement;
        import core.web.iWebElementsList;
        import org.openqa.selenium.support.FindBy;
        import pages.AbstractPage;
        import utils.iPageFactory;
        import utils.logging.iLogger;
        ```

        OUTPUT FORMAT:
        - Output ONLY the raw Java source code
        - No markdown code fences, no explanation text
        - The file must compile as-is
        """;

    @Test
    public void generateLoginPageObject() {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY environment variable is not set");
        }

        // Step 1: clean the HTML (exactly as PageCrawler would)
        String cleanedHtml = DomCleaner.clean(FIXTURE_HTML);
        iLogger.info("Cleaned HTML length: " + cleanedHtml.length() + " chars");

        // Step 2: build a minimal accessibility tree (simulated — no browser available)
        String accessibilityTree = """
            {"role":"main","children":[
              {"role":"heading","name":"Login to Your Account"},
              {"role":"textbox","name":"Username","id":"username","testid":"username-input"},
              {"role":"textbox","name":"Password","id":"password","testid":"password-input"},
              {"role":"checkbox","name":"Remember me","id":"remember-me"},
              {"role":"button","name":"Login","id":"login-button","testid":"login-submit"},
              {"role":"link","name":"Forgot your password?","id":"forgot-password-link"},
              {"role":"link","name":"Create an account","id":"register-link"}
            ]}""";

        PageSnapshot snapshot = new PageSnapshot(
            TARGET_URL,
            "Login Page",
            cleanedHtml,
            accessibilityTree,
            null
        );

        // Step 3: build the user message
        String userMessage = buildUserMessage(snapshot);
        iLogger.info("User message length: " + userMessage.length() + " chars");

        // Step 4: call Anthropic API
        AuthConfig auth = new AuthConfig(AuthConfig.AuthType.API_KEY, apiKey);
        AiProvider provider = new AnthropicProvider(auth, "claude-sonnet-4-6");
        AiRequest request = new AiRequest(SYSTEM_PROMPT, userMessage, null);

        iLogger.info("Sending request to Anthropic...");
        AiResponse response = provider.complete(request);

        // Step 5: print results
        iLogger.info("=== SMOKE TEST RESULT ===");
        iLogger.info("Model: " + response.getModel());
        iLogger.info("Tokens used: " + response.getInputTokens() + " in / " + response.getOutputTokens() + " out");
        iLogger.info("=== GENERATED PAGE OBJECT ===");

        // Print to stdout so it's clearly visible in test output
        System.out.println("\n" + "=".repeat(80));
        System.out.println("GENERATED PAGE OBJECT (claude-sonnet-4-6):");
        System.out.println("=".repeat(80));
        System.out.println(response.getContent());
        System.out.println("=".repeat(80) + "\n");
    }

    private String buildUserMessage(PageSnapshot snapshot) {
        return String.format("""
            Generate a Page Object for the following page.

            URL: %s
            Title: %s

            === CLEANED HTML ===
            %s

            === ACCESSIBILITY TREE (JSON) ===
            %s
            """,
            snapshot.getUrl(),
            snapshot.getTitle(),
            snapshot.getCleanedHtml(),
            snapshot.getAccessibilityTree()
        );
    }
}
