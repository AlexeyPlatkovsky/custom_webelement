package utils;

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
}
