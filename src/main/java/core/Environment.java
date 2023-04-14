package core;

import utils.StringUtil;
import utils.logging.iLogger;
import utils.properties.SystemProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment {
    public static String getRootUrl() {
        String rootUrl = SystemProperties.ROOT_URL;

        Pattern p = Pattern.compile("^\\w+\\.([\\w.-]+)(?::\\d+)?/?$");
        Matcher m = p.matcher(rootUrl);
        if (m.find()) {
            rootUrl = m.group(1);
            iLogger.info("Get root URL: " + rootUrl);
        }
        return StringUtil.cutExtraEndSlashes(rootUrl);
    }
}
