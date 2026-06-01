package mentoring.acomi.bookservice.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.RemoveBookCopiesRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookApiIntegrationTest {

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String LIBRARIAN_ROLE = "LIBRARIAN";

	private static final String READER_ROLE = "READER";

	private static final String ENDPOINT_ADD_COPIES = "/%s/copies/add";

	private static final String ENDPOINT_REMOVE_COPIES = "/%s/copies/remove";

	@LocalServerPort
	int port;

	private RestClient client;

	private static final String isbn = "9788804336327";

	@BeforeEach
	public void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
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
		return client.get().uri(uri).headers(header -> addUserHeaders(header, role))
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> addBook(String role) {
		AddBookRequest request = new AddBookRequest(isbn, "Italo Calvino", "Il barone rampante", "");
		return client.post().uri("/").contentType(MediaType.APPLICATION_JSON).headers(h -> addUserHeaders(h, role))
				.body(request).exchange((req, res) -> toEntity(res));
	}
	
	private ResponseEntity<String> addCopies(String role) {
		AddBookCopiesRequest request = new AddBookCopiesRequest(3);
		return client.post().uri(String.format(ENDPOINT_ADD_COPIES, isbn)).contentType(MediaType.APPLICATION_JSON).headers(h -> addUserHeaders(h, role))
				.body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> removeCopies(String role) {
		RemoveBookCopiesRequest request = new RemoveBookCopiesRequest(3, "Book Lost");
		return client.post().uri(String.format(ENDPOINT_REMOVE_COPIES, isbn)).contentType(MediaType.APPLICATION_JSON).headers(h -> addUserHeaders(h, role))
				.body(request).exchange((req, res) -> toEntity(res));
	}
	
	private void addUserHeaders(HttpHeaders headers, String role) {
		headers.set("X-User-Id", "user-1");
		headers.set("X-User-Role", role);
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

}