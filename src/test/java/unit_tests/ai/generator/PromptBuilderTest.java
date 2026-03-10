package unit_tests.ai.generator;

import ai.crawler.PageSnapshot;
import ai.generator.PromptBuilder;
import ai.provider.AiRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class PromptBuilderTest {

    private PromptBuilder builder;

    @BeforeMethod
    public void setUp() {
        builder = new PromptBuilder();
    }

    @Test
    public void systemPromptContainsMandatoryConventions() {
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("package generated"),
            "System prompt must require 'package generated' declaration");
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("AbstractPage"),
            "System prompt must mention AbstractPage");
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("iPageFactory"),
            "System prompt must mention iPageFactory");
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("iWebElement"),
            "System prompt must mention iWebElement");
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("@FindBy"),
            "System prompt must mention @FindBy");
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("@CacheElement"),
            "System prompt must mention @CacheElement");
    }

    @Test
    public void systemPromptForbidsThreadSleep() {
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("Thread.sleep"),
            "System prompt must explicitly forbid Thread.sleep()");
    }

    @Test
    public void systemPromptRequiresNoMarkdownOutput() {
        assertTrue(PromptBuilder.SYSTEM_PROMPT.contains("markdown"),
            "System prompt must prohibit markdown fences in output");
    }

    @Test
    public void userMessageContainsUrlAndTitle() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login Page",
            "<form>...</form>", "{}", null
        );
        String message = builder.buildUserMessage(snapshot);
        assertTrue(message.contains("https://example.com/login"), "User message must contain the URL");
        assertTrue(message.contains("Login Page"), "User message must contain the title");
    }

    @Test
    public void userMessageContainsCleanedHtml() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com", "Home",
            "<input id=\"search\">", "{}", null
        );
        String message = builder.buildUserMessage(snapshot);
        assertTrue(message.contains("<input id=\"search\">"), "User message must contain the cleaned HTML");
    }

    @Test
    public void userMessageContainsAccessibilityTree() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com", "Home",
            "<div/>", "{\"role\":\"main\"}", null
        );
        String message = builder.buildUserMessage(snapshot);
        assertTrue(message.contains("{\"role\":\"main\"}"), "User message must contain the accessibility tree");
    }

    @Test
    public void buildRequestPassesNullImageWhenNoScreenshot() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login",
            "<form/>", "{}", null
        );
        AiRequest request = builder.buildRequest(snapshot);
        assertNull(request.getBase64Image(), "base64Image must be null when snapshot has no screenshot");
    }

    @Test
    public void buildRequestPassesScreenshotAsBase64Image() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login",
            "<form/>", "{}", "abc123=="
        );
        AiRequest request = builder.buildRequest(snapshot);
        assertEquals(request.getBase64Image(), "abc123==",
            "base64Image must equal the snapshot screenshot data");
    }

    @Test
    public void buildRequestUsesSystemPromptConstant() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com", "Home", "<body/>", "{}", null
        );
        AiRequest request = builder.buildRequest(snapshot);
        assertEquals(request.getSystemPrompt(), PromptBuilder.SYSTEM_PROMPT,
            "System prompt in request must match the SYSTEM_PROMPT constant");
    }

    @Test
    public void buildRequestProducesNonNullUserMessage() {
        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com", "Home", "<body/>", "{}", null
        );
        AiRequest request = builder.buildRequest(snapshot);
        assertNotNull(request.getUserMessage(), "User message must not be null");
        assertTrue(request.getUserMessage().length() > 0, "User message must not be empty");
    }
}
