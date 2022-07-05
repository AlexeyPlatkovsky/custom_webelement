package core;

import utils.logging.iLogger;
import utils.properties.EnvProperties;
import utils.properties.SystemProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment {

  public static String getBaseUrl() {
    Pattern p = Pattern.compile("^\\w+\\.([\\w.-]+)(?::\\d+)?$");
    Matcher m = p.matcher(SystemProperties.BASE_URL);
    if (m.find()) {
      iLogger.info("Get base URL: " + m.group(1));
      return m.group(1);
    } else {
      iLogger.info("Get base URL: No match");
      return SystemProperties.BASE_URL;
    }
  }

  public static String getSiteUrl(String property) {
    String localPort = EnvProperties.USE_LOCAL_PORT;
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
