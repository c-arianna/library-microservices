package mentoring.acomi.librarytest.support;

import org.springframework.test.web.servlet.client.RestTestClient;

import io.cucumber.java.After;

public class Hooks {

	private static final String APPLICATION_BASE_URL = "http://localhost:8080";

	private final RestTestClient client;

	public Hooks() {
		this.client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();
	}

	@After
	public void cleanup() {
		client.post().uri("/test/reset").exchange();
	}

}
