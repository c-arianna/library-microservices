package mentoring.acomi.bookservice.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.BooksResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiIntegrationTest {

	@LocalServerPort
    int port;
	
	private RestClient client;

	@BeforeEach
	public void setup() {
		 this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d",port)).build();
	}
	
	@Test
	void shouldReturnOkWhenCallingGetBooks() {
		ResponseEntity<BooksResponse> response =  client.get().uri("/").retrieve().toEntity(BooksResponse.class);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assertions.assertNotNull(response.getBody());
	}

	@Test
	void shouldCreateBook() {
		AddBookRequest request = new AddBookRequest("9788804336327", "Italo Calvino", "Il barone rampante", "");
		ResponseEntity<String> response = client.post().uri("/").contentType(MediaType.APPLICATION_JSON)
	            .body(request).retrieve().toEntity(String.class);

		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

}