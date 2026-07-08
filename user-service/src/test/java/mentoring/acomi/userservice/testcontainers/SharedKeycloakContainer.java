package mentoring.acomi.userservice.testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public final class SharedKeycloakContainer {

    @SuppressWarnings("resource")
	public static final KeycloakContainer INSTANCE = new KeycloakContainer("quay.io/keycloak/keycloak:26.3")
                    .withRealmImportFile("keycloak/realm-export-test.json");

    static {
        INSTANCE.start();
    }

    private SharedKeycloakContainer() {}
    
}