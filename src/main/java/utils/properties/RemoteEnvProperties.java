package utils.properties;

@FilePath(value = "./src/test/resources/remoteEnv.properties")
public class RemoteEnvProperties {

    @Property(value = "env.remote_url")
    public static String REMOTE_URL_KEY;

    @Property(value = "env.use_local_port")
    public static String USE_LOCAL_PORT;
}
