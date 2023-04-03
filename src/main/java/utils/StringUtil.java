package utils;

public class StringUtil {

    /*
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

    /*
     * @param relativeUrl
     * @return relativeUrl with slash at the beginning and without extra slashes at the end
     */
    public static String formatRelativeURL(String relativeUrl) {
        relativeUrl = cutExtraEndSlashes(relativeUrl);
        if (relativeUrl.isEmpty()) {
            return "";
        }
        return (relativeUrl.startsWith("/")) ? relativeUrl : "/" + relativeUrl;
    }
}
