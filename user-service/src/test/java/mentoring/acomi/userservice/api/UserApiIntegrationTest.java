package mentoring.acomi.userservice.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import mentoring.acomi.userservice.application.security.JwtProperties;
import mentoring.acomi.userservice.infrastructure.dto.AuthResponse;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiIntegrationTest {

	@LocalServerPort
    int port;
	
	private RestClient client;
	
	@Autowired
	JwtProperties properties;

	@BeforeEach
	public void setup() {
		 this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d",port)).build();
	}
	
	@Test
	void shouldCreateUser() {
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", "test@gmail.com", "12345678");
		ResponseEntity<AuthResponse> response = client.post().uri("/auth/subscribe").contentType(MediaType.APPLICATION_JSON)
	            .body(request).retrieve().toEntity(AuthResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
		
		AuthResponse body = response.getBody();
		
		Assertions.assertNotNull(response.getBody());
		
		Assertions.assertNotNull(body.userId());
		Assertions.assertNotNull(body.accessToken());
		Assertions.assertNotNull(body.refreshToken());
	}

}