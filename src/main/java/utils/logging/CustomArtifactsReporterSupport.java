package utils.logging;

import org.jetbrains.annotations.NotNull;
import org.testng.ITestResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CustomArtifactsReporterSupport {
    public static final String SCREENSHOT_PATH_ATTRIBUTE = "testngScreenshotRelativePath";
    private static final Pattern ANCHOR_HREF_PATTERN = Pattern.compile("(?i)<a\\s+[^>]*href=['\"]([^'\"]+)['\"][^>]*>.*?</a>");
    private static final Pattern SCREENSHOT_PATH_PATTERN = Pattern.compile("(?i)(screenshots/[\\w.\\-]+\\.png)");
    private static final Pattern DATA_IMAGE_PATTERN = Pattern.compile("(?i)(data:image/[^'\"\\s>]+)");

    private CustomArtifactsReporterSupport() {
    }

    public static String normalizeReporterLogs(List<String> testLogs) {
        if (testLogs == null || testLogs.isEmpty()) {
            return "";
        }

        String normalized = String.join(System.lineSeparator(), testLogs)
                .replaceAll("(?i)<br\\s*/?>", System.lineSeparator())
                .replace("</br>", System.lineSeparator())
                .replace("<pre>", "")
                .replace("</pre>", "")
                .replaceAll("(?i)<a\\s+[^>]*href=['\"]([^'\"]+)['\"][^>]*>.*?</a>", "Screenshot: $1")
                .replaceAll("(?i)<[^>]+>", "")
                .trim();
        return unescapeCommonHtmlEntities(normalized);
    }

    public static String filterLogsForStatus(String normalizedLogs, int status) {
        if (normalizedLogs == null || normalizedLogs.isBlank()) {
            return "";
        }
        if (status != ITestResult.SUCCESS) {
            return normalizedLogs.trim();
        }

        List<String> filtered = new ArrayList<>();
        for (String line : normalizedLogs.split("\\R")) {
            if (shouldKeepForSuccessfulTest(line)) {
                filtered.add(line);
            }
        }
        return String.join(System.lineSeparator(), filtered).trim();
    }

    public static Optional<String> resolveScreenshotPath(Object screenshotAttribute, List<String> testLogs) {
        String fromAttribute = screenshotAttribute == null ? "" : String.valueOf(screenshotAttribute).trim();
        if (!fromAttribute.isBlank()) {
            return Optional.of(fromAttribute);
        }

        if (testLogs == null || testLogs.isEmpty()) {
            return Optional.empty();
        }

        for (String logLine : testLogs) {
            String line = logLine == null ? "" : logLine;
            if (line.isBlank()) {
                continue;
            }

            Optional<String> fromHref = findInAnchorHref(line);
            if (fromHref.isPresent()) {
                return fromHref;
            }

            Matcher screenshotPathMatcher = SCREENSHOT_PATH_PATTERN.matcher(line);
            if (screenshotPathMatcher.find()) {
                return Optional.of(screenshotPathMatcher.group(1));
            }

            Matcher dataImageMatcher = DATA_IMAGE_PATTERN.matcher(line);
            if (dataImageMatcher.find()) {
                return Optional.of(dataImageMatcher.group(1));
            }
        }

        return Optional.empty();
    }

    private static Optional<String> findInAnchorHref(String line) {
        Matcher anchorHrefMatcher = ANCHOR_HREF_PATTERN.matcher(line);
        while (anchorHrefMatcher.find()) {
            String href = anchorHrefMatcher.group(1).trim();
            if (href.startsWith("screenshots/") || href.startsWith("data:image/")) {
                return Optional.of(href);
            }
        }
        return Optional.empty();
    }

    public static String htmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String renderScreenshotCell(Optional<String> screenshotPath, Path outputDirectory) {
        if (screenshotPath.isEmpty()) {
            return "<span class='missing'>-</span>";
        }
        String rawPath = screenshotPath.get().trim();
        String href = resolveScreenshotHref(rawPath, outputDirectory);
        String escapedHref = htmlEscape(href);
        String label = rawPath.startsWith("data:image/") ? "Open Inline Image" : "Open Screenshot";
        String hint = rawPath.startsWith("data:image/") ? "data:image/..." : rawPath.replace('\\', '/');
        return "<a href='" + escapedHref + "' target='_blank' rel='noopener noreferrer'>" + label + "</a>"
                + "<div class='hint'>" + htmlEscape(hint) + "</div>";
    }

    public static int statusSortOrder(int status) {
        return switch (status) {
            case org.testng.ITestResult.FAILURE -> 0;
            case org.testng.ITestResult.SKIP -> 1;
            case org.testng.ITestResult.SUCCESS -> 2;
            default -> 3;
        };
    }

    public static @NotNull String statusLabel(int status) {
        return switch (status) {
            case org.testng.ITestResult.FAILURE -> "FAILED";
            case org.testng.ITestResult.SKIP -> "SKIPPED";
            case org.testng.ITestResult.SUCCESS -> "PASSED";
            default -> "UNKNOWN";
        };
    }

    private static String unescapeCommonHtmlEntities(String value) {
        return value
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }

    private static boolean shouldKeepForSuccessfulTest(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return trimmed.contains("INFO:") || trimmed.startsWith("Screenshot: ");
    }

    private static String resolveScreenshotHref(String rawPath, Path outputDirectory) {
        if (rawPath.startsWith("data:image/")) {
            return rawPath;
        }

        if (rawPath.startsWith("file:/")) {
            return rawPath;
        }

        String normalizedPath = rawPath.replace('\\', '/');
        if (normalizedPath.matches("^[a-zA-Z]:/.*")) {
            return Path.of(normalizedPath).toUri().toString();
        }

        if (normalizedPath.startsWith("/")) {
            return Path.of(normalizedPath).toUri().toString();
        }

        String relativePath = normalizedPath.startsWith("./") ? normalizedPath.substring(2) : normalizedPath;
        Path absolutePath = outputDirectory.resolve(relativePath).normalize();
        return absolutePath.toUri().toString();
    }
}
