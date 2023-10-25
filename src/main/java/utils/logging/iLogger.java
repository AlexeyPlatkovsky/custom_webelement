package utils.logging;

import core.driver.DriverFactory;
import org.apache.hc.client5.http.utils.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.Reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class iLogger {
    private static final Logger LOG = LogManager.getLogger(iLogger.class);
    private static boolean logOnlyInfo = false;
    private static boolean consoleLogOnlyInfo = false;

    public static void info(String message) {
        LOG.info(message);
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
        if (!consoleLogOnlyInfo)
            LOG.debug(message);
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
        LOG.error(message);
        Reporter.log(timeStamp() + "ERROR: " + message + "</br>");
    }

    public static void error(String s, Throwable e) {
        LOG.error(s, e);
        Reporter.log(timeStamp() + "ERROR: " + s + "</br>" + e);
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

    public static void takeScreenshot() {
        File scrFile = ((TakesScreenshot) DriverFactory.getCurrentDriver()).getScreenshotAs(OutputType.FILE);
        Reporter.log("<br><a href='data:image/png;base64," + encodeFileToBase64Binary(scrFile)
                + "'> CLICK TO SEE SCREENSHOT </a></br>");
    }

    public static void takeScreenshot(String message) {
        iLogger.info(message);
        takeScreenshot();
    }

    private static String encodeFileToBase64Binary(File file) {
        String encodedFile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedFile = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            iLogger.error("Incorrect file {} path", file.getAbsolutePath(), e);
        } catch (IOException e) {
            iLogger.error("Error during file {} reading", file.getAbsolutePath(), e);
        }

        return encodedFile;
    }

    public static void setLogOnlyInfo(boolean b) {
        logOnlyInfo = b;
    }

    public static void setConsoleLogOnlyInfo(boolean b) {
        consoleLogOnlyInfo = b;
    }
}
