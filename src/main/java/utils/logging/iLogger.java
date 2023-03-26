package utils.logging;

import core.driver.DriverFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    public static void info(String message) {
        LOG.info(message);
        Reporter.log(timeStamp() + "INFO: " + message + "</br>");
    }

    public static void info(String s, String replacement) {
        info(s.replace("{}", replacement));
    }

    public static void info(String s, String... replacement) {
        Arrays.stream(replacement).forEach(r -> s.replace("{}", r));
        info(s);
    }

    public static void info(String s, int replacement) {
        info(s, String.valueOf(replacement));
    }

    public static void info(String s, boolean checked) {
        info(s, String.valueOf(checked));
    }

    public static void info(Object nonString) {
        info(String.valueOf(nonString));
    }

    public static void debug(String message) {
        LOG.debug(message);
        Reporter.log(timeStamp() + "DEBUG: " + message + "</br>");
    }

    public static void debug(String s, String replacement) {
        debug(s.replace("{}", replacement));
    }

    public static void debug(String s, String... replacement) {
        Arrays.stream(replacement).sequential().forEach(r -> s.replace("{}", r));
        debug(s);
    }

    public static void error(String s, String replacement) {
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

    public static void error(String s, String replacement, Throwable e) {
        error(s.replace("{}", replacement), e);
    }

    public static void error(Throwable e) {
        LOG.error(e);
        Reporter.log(timeStamp() + e);
    }

    private static String timeStamp() {
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
}
