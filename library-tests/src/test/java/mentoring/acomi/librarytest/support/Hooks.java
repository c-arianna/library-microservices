package mentoring.acomi.librarytest.support;

import org.springframework.test.web.servlet.client.RestTestClient;

import io.cucumber.java.After;
import mentoring.acomi.librarytest.config.TestConfig;

public class Hooks {

	private final RestTestClient client;

	public Hooks() {
		this.client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();
	}

	@After
	public void cleanup() {
	    client.post()
	        .uri("/test/reset")
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody().isEmpty();
	}

}
