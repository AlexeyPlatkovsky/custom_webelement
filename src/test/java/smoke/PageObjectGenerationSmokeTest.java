package smoke;

import ai.crawler.DomCleaner;
import ai.crawler.PageCrawlerFacade;
import ai.crawler.PageSnapshot;
import ai.generator.GeneratedPageObject;
import ai.generator.PageObjectWriter;
import ai.provider.AnthropicProvider;
import ai.provider.AuthConfig;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.logging.iLogger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * End-to-end smoke test: static HTML fixture → DomCleaner → PageObjectGenerator → Anthropic API
 * → PageObjectWriter → printed output.
 *
 * <p>Run with: {@code ANTHROPIC_API_KEY=<key> ./gradlew test -Dsuite=smoke -PskipAllure}
 * <p>Requires: {@code ANTHROPIC_API_KEY} environment variable set to a valid key with credits.
 */
@Test(groups = "smoke")
public class PageObjectGenerationSmokeTest {

    // Realistic login page HTML with <script>/<style> noise to exercise DomCleaner
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
                <input type="text" id="username" name="username"
                       placeholder="Enter your username"
                       data-testid="username-input" required autofocus>
              </div>
              <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       placeholder="Enter your password"
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

    private static final String FIXTURE_A11Y =
        "{\"role\":\"main\",\"children\":["
        + "{\"role\":\"heading\",\"name\":\"Login to Your Account\"},"
        + "{\"role\":\"textbox\",\"name\":\"Username\",\"id\":\"username\","
        +  "\"testid\":\"username-input\"},"
        + "{\"role\":\"textbox\",\"name\":\"Password\",\"id\":\"password\","
        +  "\"testid\":\"password-input\"},"
        + "{\"role\":\"checkbox\",\"name\":\"Remember me\",\"id\":\"remember-me\"},"
        + "{\"role\":\"button\",\"name\":\"Login\",\"id\":\"login-button\","
        +  "\"testid\":\"login-submit\"},"
        + "{\"role\":\"link\",\"name\":\"Forgot your password?\","
        +  "\"id\":\"forgot-password-link\"},"
        + "{\"role\":\"link\",\"name\":\"Create an account\",\"id\":\"register-link\"}"
        + "]}";

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

    @Test
    public void generateLoginPageObject() throws Exception {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY environment variable is not set");
        }

        // Step 1: clean the HTML exactly as PageCrawler would at runtime
        String cleanedHtml = DomCleaner.clean(FIXTURE_HTML);
        iLogger.info("Cleaned HTML length: " + cleanedHtml.length() + " chars");

        PageSnapshot snapshot = new PageSnapshot(TARGET_URL, "Login Page", cleanedHtml, FIXTURE_A11Y, null);

        // Step 2: build provider and generate via the facade (snapshot → AI → GeneratedPageObject)
        AuthConfig auth = new AuthConfig(AuthConfig.AuthType.API_KEY, apiKey);
        AnthropicProvider provider = new AnthropicProvider(auth, "claude-sonnet-4-6");

        iLogger.info("Sending snapshot to Anthropic for Page Object generation...");
        GeneratedPageObject pageObject = PageCrawlerFacade.generatePageObject(snapshot, provider);

        // Step 3: write the generated source to src/test/java/generated/
        Path written = new PageObjectWriter().write(pageObject);
        iLogger.info("Page Object written to: " + written.toAbsolutePath());

        // Step 4: print the result clearly to stdout for manual inspection
        System.out.println("\n" + "=".repeat(80));
        System.out.println("GENERATED PAGE OBJECT — class: " + pageObject.getClassName());
        System.out.println("=".repeat(80));
        System.out.println(pageObject.getSourceCode());
        System.out.println("=".repeat(80) + "\n");

        iLogger.info("Written " + Files.size(written) + " bytes to " + written.getFileName());
    }
}
