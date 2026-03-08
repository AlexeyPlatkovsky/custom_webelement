package ai.provider;

import lombok.Value;

@Value
public class AuthConfig {

    public enum AuthType {
        API_KEY,
        AUTH_TOKEN
    }

    AuthType type;
    String value;
}
