package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * @param s
     * @return s without extra slashes at the end
     */
    public static String cutExtraEndSlashes(String s) {
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
            return cutExtraEndSlashes(s);
        }
        return s;
    }

    /**
     * @param url
     * @return url with slash at the beginning and without extra slashes at the end
     */
    public static String formatRelativeURL(String url) {
        url = cutExtraEndSlashes(url);
        return (url.startsWith("http") || url.startsWith("www") || url.startsWith("/")) ? url : "/" + url;
    }

    /**
     * Converts css selector to xpath
     *
     * @param css
     * @return xpath
     */
    public static String cssToXPath(String css) {
        String[] parts = css.split(" ");
        String xpath = "";

        for (String part : parts) {
            String tagName = "*";
            String condition = "";

            if (!part.startsWith(".") && !part.startsWith("#") && !part.startsWith("[")) {
                tagName = part.split("\\.|#|\\[")[0];
                part = part.replaceFirst("^" + tagName, "");
            }

            // Convert classes (.class)
            Pattern classPattern = Pattern.compile("\\.(\\w+)");
            Matcher classMatcher = classPattern.matcher(part);
            while (classMatcher.find()) {
                condition += "[contains(@class, '" + classMatcher.group(1) + "')]";
            }

            // Convert ids (#id)
            Pattern idPattern = Pattern.compile("#(\\w+)");
            Matcher idMatcher = idPattern.matcher(part);
            while (idMatcher.find()) {
                condition += "[@id='" + idMatcher.group(1) + "']";
            }

            // Convert attributes ([attr] or [attr=value] or [attr='value'])
            Pattern attrPattern = Pattern.compile("\\[(\\w+)(~?=)?('|\")?(.*?)\\3?]");
            Matcher attrMatcher = attrPattern.matcher(part);
            while (attrMatcher.find()) {
                if (attrMatcher.group(2) == null) { // [attr]
                    condition += "[@" + attrMatcher.group(1) + "]";
                } else { // [attr=value]
                    if ("~=".equals(attrMatcher.group(2))) {
                        condition += "[contains(@" + attrMatcher.group(1) + ", '" + attrMatcher.group(4) + "')]";
                    } else {
                        condition += "[@" + attrMatcher.group(1) + "='" + attrMatcher.group(4) + "']";
                    }
                }
            }
            xpath += "//" + tagName + condition;
        }

        return xpath;
    }
}
