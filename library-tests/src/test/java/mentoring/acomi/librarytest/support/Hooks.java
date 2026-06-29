package mentoring.acomi.librarytest.support;

import org.springframework.test.web.servlet.client.RestTestClient;

import io.cucumber.java.After;
import mentoring.acomi.librarytest.config.TestConfig;

public class Hooks {

	private final RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();;
	
	private final TestContext context;

	public Hooks(TestContext context) {
		this.context = context;
	}

	@After
	public void cleanup() {
		client.post().uri("/test/reset").exchange().expectStatus().isOk().expectBody().isEmpty();
		
		for(String userIdentityProviderId : context.userPrividerIdToDelete) {
			client.post().uri(String.format("/test/reset/user/%s", userIdentityProviderId)).exchange().expectStatus().isOk().expectBody().isEmpty();
		}
	}

}
