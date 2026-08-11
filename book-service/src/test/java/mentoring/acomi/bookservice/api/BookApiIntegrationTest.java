package mentoring.acomi.bookservice.api;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import mentoring.acomi.bookservice.application.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.application.dto.AddBookDto;
import mentoring.acomi.bookservice.application.dto.RemoveBookCopiesRequest;
import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookApiIntegrationTest {

	private static final String TOKEN_VALUE = "test-token";

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String LIBRARIAN_ROLE = "LIBRARIAN";

	private static final String READER_ROLE = "READER";

	private static final String ENDPOINT_ADD_COPIES = "/%s/copies/add";

	private static final String ENDPOINT_REMOVE_COPIES = "/%s/copies/remove";

	@LocalServerPort
	int port;

	private RestClient client;

	private static final String isbn = "9788804336327";

	@MockitoBean
	private EventDispatcher eventDispatcher;
	
	@MockitoBean
	private JwtDecoder jwtDecoder;
	
	@Autowired
	private BookViewRepository repository;
	
	@Autowired
	private BookEventRepository bookEventRepository;

	@BeforeEach
	public void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
	}

	@AfterEach
	void clearDb() {
		repository.deleteAll();
		bookEventRepository.deleteAll();
	}
	
	@Test
	public void readerGetBooks() {
		ResponseEntity<String> response = getWithRole("/", READER_ROLE);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void librarianCanGetBooks() {
		ResponseEntity<String> response = getWithRole("/", LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void adminCanGetBooks() {
		ResponseEntity<String> response = getWithRole("/", ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void readerCannotAddBook() {
		ResponseEntity<String> response = addBook(READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void librarianCanAddBook() {
		ResponseEntity<String> response = addBook(LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	void adminCanAddBook() {
		ResponseEntity<String> response = addBook(ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	void readerCannotAddCopies() {
		ResponseEntity<String> response = addCopies(READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void librarianCanAddCopies() {
		setupBook(LIBRARIAN_ROLE);
		ResponseEntity<String> response = addCopies(LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	void adminCanAddCopies() {
		setupBook(ADMIN_ROLE);
		ResponseEntity<String> response = addCopies(ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	void readerCannotRemoveCopies() {
		ResponseEntity<String> response = removeCopies(READER_ROLE);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void librarianCanRemoveCopies() {
		setupBook(LIBRARIAN_ROLE);
		setupCopies(LIBRARIAN_ROLE);
		ResponseEntity<String> response = removeCopies(LIBRARIAN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	void adminCanRemoveCopies() {
		setupBook(ADMIN_ROLE);
		setupCopies(ADMIN_ROLE);
		ResponseEntity<String> response = removeCopies(ADMIN_ROLE);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	private ResponseEntity<String> getWithRole(String uri, String role) {
		
		generateToken(role);
		
		return client.get().uri(uri).header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE))
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> addBook(String role) {
		
		generateToken(role);
		
		AddBookDto request = new AddBookDto(isbn, "Italo Calvino", "Il barone rampante", "");
		return client.post().uri("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE))
				.body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> addCopies(String role) {
		
		generateToken(role);
		
		AddBookCopiesRequest request = new AddBookCopiesRequest(3);
		return client.post().uri(String.format(ENDPOINT_ADD_COPIES, isbn)).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> removeCopies(String role) {
		
		generateToken(role);
		
		RemoveBookCopiesRequest request = new RemoveBookCopiesRequest(3, "Book Lost");
		return client.post().uri(String.format(ENDPOINT_REMOVE_COPIES, isbn)).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> toEntity(ClientHttpResponse response) throws IOException {
		String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
	}

	private void setupBook(String role) {
		ResponseEntity<String> response = addBook(role);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Setup failed: unable to create book");
	}

	private void setupCopies(String role) {
		ResponseEntity<String> response = addCopies(role);
		Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Setup failed: unable to add copy");
	}
	
	private void generateToken(String role) {
		
		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none").claim("email", "user@test.it")
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		when(jwtDecoder.decode(TOKEN_VALUE)).thenReturn(jwt);
	}

}