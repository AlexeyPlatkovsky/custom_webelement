package unit_tests.ai.generator;

import ai.crawler.PageSnapshot;
import ai.generator.GeneratedPageObject;
import ai.generator.PageObjectGenerator;
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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test for the full generation pipeline using the real Anthropic API.
 *
 * <p>Requires: {@code ANTHROPIC_API_KEY} environment variable set with a valid key.
 * Run with: {@code ./gradlew test -Dsuite=integration -PskipAllure}
 */
@Test(groups = "integration", singleThreaded = true)
public class PageObjectGeneratorIntegrationTest {

    private static final String TARGET_URL = "https://myapp.example.com/login";
    private static final String TARGET_TITLE = "Login Page";

    private static final String FIXTURE_HTML =
        "<html lang=\"en\"><head><title>Login Page</title></head>"
        + "<body>"
        + "<h2>Login to Your Account</h2>"
        + "<div id=\"flash-message\" class=\"error\"></div>"
        + "<form id=\"login-form\" action=\"/authenticate\" method=\"post\">"
        + "  <label for=\"username\">Username</label>"
        + "  <input type=\"text\" id=\"username\" name=\"username\""
        + "         data-testid=\"username-input\" required>"
        + "  <label for=\"password\">Password</label>"
        + "  <input type=\"password\" id=\"password\" name=\"password\""
        + "         data-testid=\"password-input\" required>"
        + "  <label>"
        + "    <input type=\"checkbox\" id=\"remember-me\" name=\"remember\"> Remember me"
        + "  </label>"
        + "  <button type=\"submit\" id=\"login-button\" data-testid=\"login-submit\">Login</button>"
        + "</form>"
        + "<a href=\"/forgot-password\" id=\"forgot-password-link\">Forgot your password?</a>"
        + "<a href=\"/register\" id=\"register-link\">Create an account</a>"
        + "</body></html>";

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
    public void configureProxyAuthAndVerifyKey() {
        // Register proxy authenticator so HttpClient can traverse the sandbox outbound proxy
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

        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                "ANTHROPIC_API_KEY environment variable is required for integration tests"
            );
        }
    }

    private AnthropicProvider buildProvider() {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        AuthConfig auth = new AuthConfig(AuthConfig.AuthType.API_KEY, apiKey);
        return new AnthropicProvider(auth, "claude-sonnet-4-6");
    }

    private PageSnapshot loginPageSnapshot() {
        return new PageSnapshot(TARGET_URL, TARGET_TITLE, FIXTURE_HTML, FIXTURE_A11Y, null);
    }

    @Test
    public void generatedSourceContainsPackageDeclaration() {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        assertNotNull(result.getSourceCode(), "Source code must not be null");
        assertTrue(result.getSourceCode().contains("package generated;"),
            "Generated source must have 'package generated;' declaration.\nActual:\n" + result.getSourceCode());
    }

    @Test
    public void generatedSourceExtendsAbstractPage() {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        assertTrue(result.getSourceCode().contains("extends AbstractPage"),
            "Generated class must extend AbstractPage.\nActual:\n" + result.getSourceCode());
    }

    @Test
    public void generatedSourceContainsPageUrlAnnotation() {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        assertTrue(result.getSourceCode().contains("@PageURL"),
            "Generated class must have @PageURL annotation.\nActual:\n" + result.getSourceCode());
    }

    @Test
    public void generatedSourceUsesIWebElement() {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        assertTrue(result.getSourceCode().contains("iWebElement"),
            "Generated source must use iWebElement, not raw WebElement.\nActual:\n" + result.getSourceCode());
        assertFalse(result.getSourceCode().contains("WebElement "),
            "Generated source must not use raw WebElement type.\nActual:\n" + result.getSourceCode());
    }

    @Test
    public void generatedSourceContainsFindByAnnotations() {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        assertTrue(result.getSourceCode().contains("@FindBy"),
            "Generated source must use @FindBy annotations.\nActual:\n" + result.getSourceCode());
    }

    @Test
    public void extractedClassNameIsValidJavaIdentifier() {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        String className = result.getClassName();
        assertNotNull(className, "Class name must not be null");
        assertFalse(className.isBlank(), "Class name must not be blank");
        assertTrue(Character.isUpperCase(className.charAt(0)),
            "Class name must start with an uppercase letter, got: " + className);
        iLogger.info("Extracted class name: " + className);
    }

    @Test
    public void generatedPageObjectCanBeWrittenToDisk() throws Exception {
        GeneratedPageObject result = new PageObjectGenerator(buildProvider()).generate(loginPageSnapshot());

        Path tempDir = Files.createTempDirectory("integration-test-generated-");
        try {
            Path written = new PageObjectWriter(tempDir).write(result);
            assertTrue(Files.exists(written), "Written file must exist on disk");
            assertTrue(Files.size(written) > 0, "Written file must not be empty");
            iLogger.info("Integration test wrote Page Object to: " + written);

            // Print to stdout for manual inspection
            System.out.println("\n" + "=".repeat(72));
            System.out.println("GENERATED PAGE OBJECT — " + result.getClassName());
            System.out.println("=".repeat(72));
            System.out.println(result.getSourceCode());
            System.out.println("=".repeat(72) + "\n");
        } finally {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception ignored) {
                        // best-effort cleanup
                    }
                });
            }
        }
    }
}
