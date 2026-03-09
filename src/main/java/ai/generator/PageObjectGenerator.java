package ai.generator;

import ai.crawler.PageSnapshot;
import ai.provider.AiProvider;
import ai.provider.AiRequest;
import ai.provider.AiResponse;
import utils.logging.iLogger;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a Page Object Java source by sending a {@link PageSnapshot} to an {@link AiProvider}.
 *
 * <p>Usage:
 * <pre>{@code
 * PageObjectGenerator generator = new PageObjectGenerator(provider);
 * GeneratedPageObject result = generator.generate(snapshot);
 * }</pre>
 */
public class PageObjectGenerator {

    private static final Pattern CLASS_NAME_PATTERN =
        Pattern.compile("public\\s+class\\s+(\\w+)");

    private final AiProvider provider;
    private final PromptBuilder promptBuilder;

    public PageObjectGenerator(AiProvider provider) {
        this(provider, new PromptBuilder());
    }

    public PageObjectGenerator(AiProvider provider, PromptBuilder promptBuilder) {
        this.provider = provider;
        this.promptBuilder = promptBuilder;
    }

    /**
     * Sends {@code snapshot} to the AI provider and returns the generated Page Object.
     *
     * @throws RuntimeException if the AI call fails or the response cannot be parsed
     */
    public GeneratedPageObject generate(PageSnapshot snapshot) {
        iLogger.info("Generating Page Object for: " + snapshot.getUrl());

        AiRequest request = promptBuilder.buildRequest(snapshot);
        AiResponse response = provider.complete(request);

        iLogger.info("Received response: model=" + response.getModel()
            + ", tokens=" + response.getInputTokens() + "/" + response.getOutputTokens());

        String sourceCode = stripCodeFences(response.getContent());
        validateSource(sourceCode, snapshot.getUrl());
        sourceCode = injectPackageIfMissing(sourceCode);
        String className = extractClassName(sourceCode, snapshot.getUrl());

        iLogger.info("Extracted class name: " + className);
        return new GeneratedPageObject(className, sourceCode, snapshot);
    }

    /**
     * Validates that the AI response looks like Java source code.
     * Throws {@link IllegalStateException} if the class declaration is absent (model returned
     * an error message or the response was truncated) or if markdown fences were not fully removed.
     * Public so unit tests in a different package can exercise this method directly.
     */
    public void validateSource(String javaSource, String url) {
        if (!javaSource.contains("class ")) {
            throw new IllegalStateException(
                "AI response for URL '" + url + "' does not contain a class declaration. "
                + "The model may have returned an error or the response was truncated."
            );
        }
        if (javaSource.contains("```")) {
            throw new IllegalStateException(
                "AI response for URL '" + url + "' still contains markdown fences after extraction."
            );
        }
    }

    /**
     * Ensures the source starts with {@code package generated;}.
     * The AI occasionally omits the package declaration despite explicit instructions.
     * Leading blank lines are stripped before the check to avoid a double declaration.
     * Public so unit tests in a different package can exercise this method directly.
     */
    public String injectPackageIfMissing(String javaSource) {
        String stripped = javaSource.stripLeading();
        if (!stripped.startsWith("package ")) {
            iLogger.error("PageObjectGenerator: AI omitted package declaration — injecting 'package generated;'");
            return "package generated;\n\n" + stripped;
        }
        return stripped;
    }

    /**
     * Strips Markdown code fences that the model may wrap around the output despite instructions.
     * Handles both {@code ```java ... ```} and plain {@code ``` ... ```} wrapping.
     * Public so unit tests in a different package can exercise this method directly.
     */
    public String stripCodeFences(String raw) {
        // Only process if the response (ignoring leading whitespace) starts with a fence
        if (!raw.stripLeading().startsWith("```")) {
            return raw; // No fences — return the original content unchanged
        }
        int firstNewline = raw.indexOf('\n');
        if (firstNewline < 0) {
            return raw;
        }
        String afterOpenFence = raw.substring(firstNewline + 1);
        // Locate closing ``` — it appears as the last "```" on a line of its own
        String contentStripped = afterOpenFence.stripTrailing();
        if (contentStripped.endsWith("```")) {
            int lastFenceIdx = contentStripped.lastIndexOf("```");
            return contentStripped.substring(0, lastFenceIdx).stripTrailing();
        }
        return afterOpenFence;
    }

    /**
     * Extracts the simple class name from the generated source.
     * Falls back to a name derived from the URL path if extraction fails.
     * Public so unit tests in a different package can exercise this method directly.
     */
    public String extractClassName(String sourceCode, String url) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        iLogger.error("Could not extract class name from generated source; deriving from URL: " + url);
        return deriveClassNameFromUrl(url);
    }

    private String deriveClassNameFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            if (path == null || path.isBlank() || path.equals("/")) {
                return "HomePage";
            }
            String[] segments = path.replaceAll("^/+|/+$", "").split("/");
            StringBuilder name = new StringBuilder();
            for (String segment : segments) {
                if (!segment.isBlank()) {
                    name.append(Character.toUpperCase(segment.charAt(0)));
                    name.append(segment.substring(1).toLowerCase());
                }
            }
            String base = name.toString().replaceAll("[^A-Za-z0-9]", "");
            return base.isEmpty() ? "UnknownPage" : base + "Page";
        } catch (IllegalArgumentException e) {
            return "UnknownPage";
        }
    }
}
