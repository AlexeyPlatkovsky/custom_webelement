package unit_tests.ai.generator;

import ai.crawler.PageSnapshot;
import ai.generator.GeneratedPageObject;
import ai.generator.PageObjectGenerator;
import ai.generator.PromptBuilder;
import ai.provider.AiProvider;
import ai.provider.AiRequest;
import ai.provider.AiResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "unit", singleThreaded = true)
public class PageObjectGeneratorTest {

    private static final String VALID_SOURCE =
        "package generated;\n\n"
        + "import core.annotations.PageURL;\n"
        + "import org.openqa.selenium.support.FindBy;\n"
        + "import pages.AbstractPage;\n\n"
        + "@PageURL(\"https://example.com/login\")\n"
        + "public class LoginPage extends AbstractPage {\n"
        + "}\n";

    private AiProvider mockProvider;

    @BeforeMethod
    public void setUp() {
        mockProvider = mock(AiProvider.class);
    }

    @Test
    public void generateReturnsCorrectClassNameFromSource() {
        when(mockProvider.complete(any(AiRequest.class)))
            .thenReturn(new AiResponse(VALID_SOURCE, "test-model", 100, 200));

        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login", "<form/>", "{}", null
        );
        GeneratedPageObject result = new PageObjectGenerator(mockProvider).generate(snapshot);

        assertEquals(result.getClassName(), "LoginPage",
            "Class name should be extracted from the 'public class' declaration");
    }

    @Test
    public void generatePreservesSourceCode() {
        when(mockProvider.complete(any(AiRequest.class)))
            .thenReturn(new AiResponse(VALID_SOURCE, "test-model", 100, 200));

        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login", "<form/>", "{}", null
        );
        GeneratedPageObject result = new PageObjectGenerator(mockProvider).generate(snapshot);

        assertEquals(result.getSourceCode(), VALID_SOURCE,
            "Source code should be the cleaned AI response");
    }

    @Test
    public void generateAttachesOriginalSnapshot() {
        when(mockProvider.complete(any(AiRequest.class)))
            .thenReturn(new AiResponse(VALID_SOURCE, "test-model", 100, 200));

        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login", "<form/>", "{}", null
        );
        GeneratedPageObject result = new PageObjectGenerator(mockProvider).generate(snapshot);

        assertEquals(result.getSnapshot(), snapshot,
            "Generated page object must reference the original snapshot");
    }

    @Test
    public void generateUsesCustomPromptBuilder() {
        PromptBuilder customBuilder = mock(PromptBuilder.class);
        when(customBuilder.buildRequest(any())).thenReturn(
            new AiRequest("custom-system", "custom-user", null)
        );
        when(mockProvider.complete(any(AiRequest.class)))
            .thenReturn(new AiResponse(VALID_SOURCE, "test-model", 50, 100));

        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com", "Home", "<body/>", "{}", null
        );
        GeneratedPageObject result = new PageObjectGenerator(mockProvider, customBuilder).generate(snapshot);

        assertNotNull(result, "Result must not be null when custom PromptBuilder is used");
    }

    // ── stripCodeFences ───────────────────────────────────────────────────────

    @Test
    public void stripCodeFencesRemovesJavaFencedBlock() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String input = "```java\npublic class Foo {}\n```";
        assertEquals(generator.stripCodeFences(input), "public class Foo {}");
    }

    @Test
    public void stripCodeFencesRemovesPlainFencedBlock() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String input = "```\npublic class Foo {}\n```";
        assertEquals(generator.stripCodeFences(input), "public class Foo {}");
    }

    @Test
    public void stripCodeFencesLeavesCleanCodeUnchanged() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String input = "public class Foo {}";
        assertEquals(generator.stripCodeFences(input), input);
    }

    @Test
    public void stripCodeFencesHandlesLeadingTrailingWhitespace() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String input = "  ```java\npublic class Bar {}\n```  ";
        assertEquals(generator.stripCodeFences(input), "public class Bar {}");
    }

    // ── extractClassName ──────────────────────────────────────────────────────

    @Test
    public void extractClassNameFindsPublicClassDeclaration() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "package generated;\npublic class DashboardPage extends AbstractPage {}";
        assertEquals(generator.extractClassName(source, "https://x.com/dashboard"), "DashboardPage");
    }

    @Test
    public void extractClassNameFallsBackToUrlPathWhenNoClassFound() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "// no class here";
        String result = generator.extractClassName(source, "https://example.com/checkout");
        assertTrue(result.endsWith("Page"),
            "Fallback class name derived from URL should end with 'Page', got: " + result);
        assertTrue(result.length() > 4,
            "Fallback class name should be longer than just 'Page', got: " + result);
    }

    @Test
    public void extractClassNameReturnsUnknownPageForRootPath() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "not valid java";
        String result = generator.extractClassName(source, "https://example.com/");
        assertEquals(result, "HomePage",
            "Root URL path should produce 'HomePage' as fallback");
    }

    @Test
    public void extractClassNameReturnsUnknownPageWhenPathIsAllNonAlphanumeric() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "not valid java";
        // Path "---" contains only dashes; after stripping non-alphanumeric chars the base is empty
        String result = generator.extractClassName(source, "https://example.com/---");
        assertEquals(result, "UnknownPage",
            "Path composed of only non-alphanumeric characters should produce 'UnknownPage'");
    }

    // ── validateSource ────────────────────────────────────────────────────────

    @Test(expectedExceptions = IllegalStateException.class)
    public void validateSourceThrowsWhenNoClassDeclaration() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        generator.validateSource("This is an error message from the AI.", "https://example.com/login");
    }

    @Test
    public void validateSourceExceptionMessageContainsUrl() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        try {
            generator.validateSource("AI returned an error message.", "https://example.com/login");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("https://example.com/login"),
                "Exception message must include the URL");
            return;
        }
        throw new AssertionError("Expected IllegalStateException was not thrown");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void validateSourceThrowsWhenFencesRemain() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        generator.validateSource("```java\npublic class Foo {}\n```", "https://example.com");
    }

    @Test
    public void validateSourcePassesForValidSource() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        generator.validateSource(VALID_SOURCE, "https://example.com/login");
        // no exception expected
    }

    // ── injectPackageIfMissing ────────────────────────────────────────────────

    @Test
    public void injectPackageIfMissingLeavesSourceWithPackageUnchanged() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "package generated;\n\npublic class Foo extends AbstractPage {}";
        assertEquals(generator.injectPackageIfMissing(source), source);
    }

    @Test
    public void injectPackageIfMissingPrependsPackageWhenAbsent() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "public class Foo extends AbstractPage {}";
        String result = generator.injectPackageIfMissing(source);
        assertTrue(result.startsWith("package generated;\n\n"),
            "Package declaration must be prepended");
        assertTrue(result.contains("public class Foo"),
            "Original source must be preserved after injection");
    }

    @Test
    public void injectPackageIfMissingStripsLeadingBlankLines() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "\n\npackage generated;\n\npublic class Foo extends AbstractPage {}";
        String result = generator.injectPackageIfMissing(source);
        assertEquals(result, "package generated;\n\npublic class Foo extends AbstractPage {}",
            "Leading blank lines before package declaration must be stripped");
    }

    @Test
    public void injectPackageIfMissingNoDoublePackageWhenLeadingBlanks() {
        PageObjectGenerator generator = new PageObjectGenerator(mockProvider);
        String source = "\n\npackage generated;\n\npublic class Foo extends AbstractPage {}";
        String result = generator.injectPackageIfMissing(source);
        long packageCount = result.lines().filter(l -> l.startsWith("package ")).count();
        assertEquals(packageCount, 1L, "There must be exactly one package declaration");
    }

    // ── integration: fenced response is correctly unwrapped ──────────────────

    @Test
    public void generateStripsMarkdownFencesFromAiResponse() {
        String fencedSource = "```java\n" + VALID_SOURCE + "\n```";
        when(mockProvider.complete(any(AiRequest.class)))
            .thenReturn(new AiResponse(fencedSource, "test-model", 100, 200));

        PageSnapshot snapshot = new PageSnapshot(
            "https://example.com/login", "Login", "<form/>", "{}", null
        );
        GeneratedPageObject result = new PageObjectGenerator(mockProvider).generate(snapshot);

        // stripCodeFences removes fences and trailing whitespace; trailing \n in VALID_SOURCE is removed
        assertEquals(result.getSourceCode(), VALID_SOURCE.stripTrailing(),
            "Markdown fences must be stripped from the generated source");
        assertEquals(result.getClassName(), "LoginPage",
            "Class name must still be correctly extracted after fence removal");
    }
}
