package mentoring.acomi.userservice.testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public abstract class AbstractKeycloakIntegrationTest {

    protected static final KeycloakContainer KEYCLOAK = SharedKeycloakContainer.INSTANCE;

    @DynamicPropertySource
    static void keycloakProps(DynamicPropertyRegistry registry) {

        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "%s/realms/library-microservices/protocol/openid-connect/certs".formatted(KEYCLOAK.getAuthServerUrl()));
        registry.add("keycloak.base-url", KEYCLOAK::getAuthServerUrl);
        registry.add("keycloak.realm", () -> "library-microservices");
        registry.add("keycloak.admin-realm", () -> "library-microservices");
        registry.add("keycloak.admin-client-id", () -> "user-service-admin");
        registry.add("keycloak.admin-client-secret", () -> "test-secret");
    }
}