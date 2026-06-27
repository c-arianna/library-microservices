package mentoring.acomi.librarytest.config;

public class TestConfig {

    public static final String BASE_URL;
    public static final String KEYCLOAK_URL;
    public static final String REALM;
    public static final String CLIENT_ID;

    static {
        BASE_URL = getEnv("BASE_URL", "http://localhost:9080");
        KEYCLOAK_URL = getEnv("KEYCLOAK_URL", "http://localhost:8084");
        REALM = getEnv("KEYCLOAK_REALM", "library-microservices-test");
        CLIENT_ID = getEnv("CLIENT_ID", "library-test-client");

        validate();
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    private static void validate() {
        if (!BASE_URL.contains("9080")) {
            throw new IllegalStateException("Tests must run against GHERKIN environment! Current BASE_URL=".formatted(BASE_URL));
        }
    }
}