package ai.provider;

public class UnsupportedAuthException extends RuntimeException {

    public UnsupportedAuthException(String providerName, AuthConfig.AuthType attempted) {
        super(String.format(
            "Provider '%s' does not support auth type %s. Check ai-provider.properties.",
            providerName, attempted
        ));
    }
}
