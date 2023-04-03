package core;

import utils.StringUtil;
import utils.logging.iLogger;
import utils.properties.RemoteEnvProperties;
import utils.properties.SystemProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment {

    public static String getBaseUrl() {
        String baseUrl = SystemProperties.BASE_URL;

        Pattern p = Pattern.compile("^\\w+\\.([\\w.-]+)(?::\\d+)?/?$");
        Matcher m = p.matcher(baseUrl);
        if (m.find()) {
            baseUrl = m.group(1);
            iLogger.info("Get base URL: " + baseUrl);
        }
        return StringUtil.cutExtraEndSlashes(baseUrl);
    }

    public static String getSiteUrl(String property) {
        String localPort = RemoteEnvProperties.USE_LOCAL_PORT;
        String http;
        String portColon;

        if (localPort.isEmpty()) {
            http = "https";
            portColon = "";
        } else {
            http = "http";
            portColon = ":";
        }

        return String.format("%s://%s%s%s", http, property, portColon, localPort);
    }
}
