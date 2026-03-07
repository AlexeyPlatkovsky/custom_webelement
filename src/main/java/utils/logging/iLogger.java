package utils.logging;

import core.driver.DriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.utils.Base64;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public class iLogger {
    private static boolean logOnlyInfo = true;
    private static boolean consoleLogOnlyInfo = false;
    private static final String SCREENSHOT_LINK_TEXT = " CLICK TO SEE SCREENSHOT ";
    private static final String SCREENSHOT_DIR_NAME = "screenshots";
    private static final String SCREENSHOT_RELATIVE_PREFIX = SCREENSHOT_DIR_NAME + "/";
    private static final ThreadLocal<List<String>> DEBUG_LOG_BUFFER = ThreadLocal.withInitial(ArrayList::new);

    public record ScreenshotArtifact(String fileName, Path filePath, String reportRelativePath, byte[] bytes) {
    }

    public static void info(String message) {
        log.info(message);
        Reporter.log(timeStamp() + "INFO: " + message + "</br>");
    }

    /**
     * @param s           - string with {} as a placeholders for replacements
     * @param replacement
     */
    public static void info(String s, String... replacement) {
        if (!s.contains("{}")) {
            throw new IllegalArgumentException("Incorrect replacement pattern for string: " + s
                    + ", please, use {} as a placeholder");
        }
        for (var r : replacement) {
            s = s.replaceFirst("\\{}", r);
        }
        info(s);
    }


    public static void info(String s, int replacement) {
        info(s, String.valueOf(replacement));
    }

    public static void info(String s, boolean replacement) {
        info(s, String.valueOf(replacement));
    }

    public static void info(Object nonString) {
        info(String.valueOf(nonString));
    }

    public static void debug(String message) {
        DEBUG_LOG_BUFFER.get().add(timeStamp() + "DEBUG: " + message);
        if (!consoleLogOnlyInfo)
            log.debug(message);
        if (!logOnlyInfo)
            Reporter.log(timeStamp() + "DEBUG: " + message + "</br>");
    }

    /**
     * @param s           - string with {} as a placeholders for replacements
     * @param replacement
     */
    public static void debug(@NotNull String s, String replacement) {
        debug(s.replace("{}", replacement));
    }

    public static void debug(String s, String... replacement) {
        Arrays.stream(replacement).sequential().forEach(r -> s.replace("{}", r));
        debug(s);
    }

    /**
     * @param s           - string with {} as a placeholders for replacements
     * @param replacement
     */
    public static void error(@NotNull String s, String replacement) {
        error(s.replace("{}", replacement));
    }

    public static void error(String message) {
        log.error(message);
        Reporter.log(timeStamp() + "ERROR: " + message + "</br>");
    }

    public static void error(String s, Throwable e) {
        log.error(s, e);
        Reporter.log(timeStamp() + "ERROR: " + escapeHtml(s) + "</br><pre>" + escapeHtml(stackTraceToString(e)) + "</pre>");
    }

    public static void error(@NotNull String s, String replacement, Throwable e) {
        error(s.replace("{}", replacement), e);
    }

    public static void error(Throwable e) {
        error(e.getMessage(), e);
    }

    private static @NotNull String timeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ": ";
    }

    public static ScreenshotArtifact takeScreenshot() {
        WebDriver currentDriver = DriverFactory.getCurrentDriverOrNull();
        if (currentDriver == null) {
            log.warn("Screenshot skipped: no active WebDriver session");
            return null;
        }

        if (!(currentDriver instanceof TakesScreenshot screenshotDriver)) {
            log.warn("Screenshot skipped: driver {} does not support screenshots", currentDriver.getClass().getName());
            return null;
        }

        byte[] screenshotBytes;
        try {
            screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
        } catch (Throwable throwable) {
            iLogger.error("Failed to capture screenshot bytes", throwable);
            return null;
        }

        String screenshotName = "screenshot_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date())
                + "_" + Thread.currentThread().threadId() + ".png";
        Path screenshotDir = Path.of(System.getProperty("user.dir"), "build", "reports", "tests", "testng", SCREENSHOT_DIR_NAME);
        Path screenshotPath = screenshotDir.resolve(screenshotName);
        String relativePath = SCREENSHOT_RELATIVE_PREFIX + screenshotName;

        try {
            Files.createDirectories(screenshotDir);
            Files.write(screenshotPath, screenshotBytes);
            Reporter.log("<br><a href='" + relativePath + "' target='_blank'>" + SCREENSHOT_LINK_TEXT + "</a></br>");
            return new ScreenshotArtifact(screenshotName, screenshotPath, relativePath, screenshotBytes);
        } catch (IOException e) {
            String inlineDataImage = "data:image/png;base64," + encodeFileToBase64Binary(screenshotBytes);
            Reporter.log("<br><a href='" + inlineDataImage
                    + "' target='_blank'>" + SCREENSHOT_LINK_TEXT + "</a></br>");
            iLogger.error("Failed to save screenshot to file " + screenshotPath, e);
            return new ScreenshotArtifact(screenshotName, screenshotPath, inlineDataImage, screenshotBytes);
        }
    }

    private static String encodeFileToBase64Binary(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
    }

    public static String stackTraceToString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }

    private static String escapeHtml(String value) {
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

    public static void setConsoleLogOnlyInfo(boolean b) {
        consoleLogOnlyInfo = b;
    }

    public static void setLogOnlyInfo(boolean value) {
        logOnlyInfo = value;
    }

    public static void clearDebugBuffer() {
        DEBUG_LOG_BUFFER.remove();
    }

    public static void flushDebugBufferToReporter() {
        List<String> debugLines = DEBUG_LOG_BUFFER.get();
        for (String debugLine : debugLines) {
            Reporter.log(escapeHtml(debugLine) + "</br>");
        }
    }
}
