package mentoring.acomi.librarytest.steps.books;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.jayway.jsonpath.JsonPath;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mentoring.acomi.librarytest.clients.AuthClient;
import mentoring.acomi.librarytest.support.ExpectedValue;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class BookSteps {

	private static final String APPLICATION_BASE_URL = "http://localhost:8080";
	private static final String KEYCLOAK_BASE_URL = "http://localhost:8084";
	private static final String CLIENT_ID = "library-test-client";
	private static final String KEYCLOAK_REALM = "library-microservices";

	private static final String ADMIN_ACCESS_TOKEN = "adminAccessToken";
	private static final String USER_ACCESS_TOKEN = "userAccessToken";
	private static final String RESPONSE_BODY = "responseBody";
	private static final String RESPONSE_STATUS = "responseStatus";
	
	private final TestContext context;

	private RestTestClient client;

	public BookSteps(TestContext context) {
		this.context = context;
	}

	/*
	 * ############################### GIVEN #####################################
	 */

	@Given("l'amministratore con credenziali {string}, {string} è autenticato")
	public void loginAdmin(String username, String password) {
		String accessToken = login(username, password);
		context.put(ADMIN_ACCESS_TOKEN, accessToken);
	}

	@Given("l'utente con credenziali {string}, {string} è autenticato")
	public void loginReader(String username, String password) {
		String accessToken = login(username, password);
		context.put(USER_ACCESS_TOKEN, accessToken);
	}

	@Given("l'amministratore aggiunge un libro con isbn {string}, autore {string}, titolo {string} e descrizione")
	public void addBook(String isbn, String author, String title, DocString description) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		String body = """
				{
				  "isbn": "%s",
				  "author": "%s",
				  "title": "%s",
				  "description": "%s"
				}
				""".formatted(isbn, author, title, description.getContent());

		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		var result = client.post().uri("/books").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).body(body).exchange().expectBody()
				.returnResult();

		Assertions.assertEquals(201, result.getStatus().value());
	}

	@Given("l'amministratore aggiunge {int} copie del libro {string}")
	public void addBookCopies(int quantity, String isbn) {
		
		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		String body = """
				{
				  "quantity": %d
				}
				""".formatted(quantity);

		var result = addBookCopies(isbn, accessToken, body);

		Assertions.assertEquals(204, result.getStatus().value());
	}
	
	@Given("l'amministratore rimuove una copia del libro {string}")
	public void removeCopies(String isbn) {
		
		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		String body = """
				{
				  "quantity": 1,
				  "reason": "Book Lost"
				}
				""";

		var result = removeBookCopies(isbn, accessToken, body);

		Assertions.assertEquals(204, result.getStatus().value());
	}
	
	/*
	 * ############################### WHEN #####################################
	 */

	@When("l'amministratore aggiunge un libro al catalogo con i seguenti dati:")
	public void createBook(DocString body) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		var result = client.post().uri("/books").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).body(body.getContent()).exchange()
				.expectBody().returnResult();

		context.put(RESPONSE_STATUS, result.getStatus().value());
		context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
	}

	@When("l'utente visualizza il catalogo dei libri")
	public void getBooks() {

		String accessToken = context.get(USER_ACCESS_TOKEN, String.class);

		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		var result = client.get().uri("/books").header("Authorization", String.join(" ", "Bearer", accessToken))
				.exchange().expectBody().returnResult();

		context.put(RESPONSE_STATUS, result.getStatus().value());
		context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}
	
	@When("l'utente visualizza il catalogo dei libri, con filtro di ricerca")
	public void findBooksFilter(Map<String, String> rawFilters) {

		String accessToken = context.get(USER_ACCESS_TOKEN, String.class);
		
		Map<String, String> filters = new LinkedHashMap<>();
		rawFilters.forEach((k, v) -> filters.put(k, Helper.normalize(v)));
		
		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/books");
		filters.forEach(uri::queryParam);

		var result = client.get().uri(uri.build().toUri())
				.header("Authorization", String.join(" ", "Bearer", accessToken))
				.exchange().expectBody().returnResult();

		context.put(RESPONSE_STATUS, result.getStatus().value());
		context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}

	@When("l'amministratore aggiunge una copia del libro {string}, con i seguenti dati:")
	public void addBookCopies(String isbn, DocString body) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		var result = addBookCopies(isbn, accessToken, body.getContent());

		context.put(RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
		}

	}
	
	@When("l'amministratore rimuove copie del libro {string}, con i seguenti dati:")
	public void removeBookCopies(String isbn, DocString body) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		var result = removeBookCopies(isbn, accessToken, body.getContent());

		context.put(RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
		}

	}
	
	@When("l'utente visualizza il dettaglio del libro isbn {string}")
	public void getBookDetail(String isbn) {

		String accessToken = context.get(USER_ACCESS_TOKEN, String.class);

		var result = getBookDetail(isbn, accessToken);

		context.put(RESPONSE_STATUS, result.getStatus().value());
	    context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}
	
	/*
	 * ############################### THEN #####################################
	 */

	@Then("la risposta ha status code {int}")
	public void checkStatusCode(int expectedStatus) {
		int actualStatus = context.get(RESPONSE_STATUS, Integer.class);
		Assertions.assertEquals(expectedStatus, actualStatus);
	}

	@Then("la risposta contiene il campo {string}")
	public void responseContainsField(String field) {
		String body = context.get(RESPONSE_BODY, String.class);
		var bodyResponse = JsonPath.parse(body);
		Object value = bodyResponse.read(String.format("$.%s", field));
		Assertions.assertNotNull(value, String.format("Missing field: %s", field));
	}

	@Then("il libro {string} ha totalCopies = {int}, borrowedCopies = {int}, reservedCopies = {int}")
	public void checkBookView(String isbn, int totalCopies, int borrowedCopies, int reservedCopies) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		var result = getBookDetail(isbn, accessToken);

		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
		var bodyResponse = JsonPath.parse(response);

		Assertions.assertEquals(200, result.getStatus().value());

		Integer responseTotalCopies = bodyResponse.read("$.totalCopies");
		Integer responseBorrowedCopies = bodyResponse.read("$.borrowedCopies");
		Integer responseReservedCopies = bodyResponse.read("$.reservedCopies");

		Assertions.assertEquals(totalCopies, responseTotalCopies);
		Assertions.assertEquals(borrowedCopies, responseBorrowedCopies);
		Assertions.assertEquals(reservedCopies, responseReservedCopies);

	}

	@Then("la risposta contiene i seguenti campi:")
	public void checkResponseContent(Map<String, String> expectedRaw) {

		String body = context.get(RESPONSE_BODY, String.class);
		var responseBody = JsonPath.parse(body);

		Map<String, ExpectedValue> expectedContent = new LinkedHashMap<>();
		expectedRaw.forEach((k, v) -> {
			String resolved = Helper.resolve(v, context);
			expectedContent.put(k, Helper.normalizeExpected(resolved));
		});

		expectedContent.forEach((key, expectedValue) -> {
			Object actualValue = responseBody.read(String.format("$.%s", key));
			Assertions.assertNotNull(actualValue, String.format("Missing field in response: %s", key));
			Assertions.assertTrue(expectedValue.matches(actualValue),
					String.format("Mismatch on field: %s, actual value: %s", key, actualValue));
		});

	}

	@Then("{string} è una lista vuota")
	public void checkEmptyList(String field) {
		String body = context.get(RESPONSE_BODY, String.class);
		var responseBody = JsonPath.parse(body);
		Integer size = responseBody.read(String.format("$.%s.length()", field));
		Assertions.assertEquals(0, size, String.format("'%s' is not empty", field));
	}

	@Then("{string} contiene {int} elementi")
	public void checkList(String field, int size) {
		String body = context.get(RESPONSE_BODY, String.class);
		var responseBody = JsonPath.parse(body);
		Integer actualSize = responseBody.read(String.format("$.%s.length()", field));
		Assertions.assertEquals(size, actualSize,
				String.format("Expected %d elements in '%s', found %d", size, field, actualSize));
	}

	@Then("{string} ha un elemento con i campi:")
	public void checkContentList(String field, Map<String, String> expectedRaw) {

		Map<String, ExpectedValue> expected = new LinkedHashMap<>();
		expectedRaw.forEach((k, v) -> {
			String resolved = Helper.resolve(v, context);
			expected.put(k, Helper.normalizeExpected(resolved));
		});

		String body = context.get(RESPONSE_BODY, String.class);
		var responseBody = JsonPath.parse(body);
		List<Map<String, Object>> items = responseBody.read(String.format("$.%s", field));

		boolean found = items.stream().anyMatch(
				item -> expected.entrySet().stream().allMatch(e -> e.getValue().matches(item.get(e.getKey()))));

		Assertions.assertTrue(found,
				String.format("No items in the '%s' field match the expected values: %s", field, expected));

	}

	/*
	 * ############################### HELPER METHODS #####################################
	 */

	private String login(String username, String password) {

		AuthClient authClient = new AuthClient(KEYCLOAK_BASE_URL, KEYCLOAK_REALM);

		return authClient.login(CLIENT_ID, username, password);
		
	}
	
	private EntityExchangeResult<byte[]> addBookCopies(String isbn, String accessToken, String body) {
		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		return client.post().uri(String.format("/books/%s/copies/add", isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}
	
	private EntityExchangeResult<byte[]> removeBookCopies(String isbn, String accessToken, String body) {
		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		return client.post().uri(String.format("/books/%s/copies/remove", isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}
	
	private EntityExchangeResult<byte[]> getBookDetail(String isbn, String accessToken) {
		client = RestTestClient.bindToServer().baseUrl(APPLICATION_BASE_URL).build();

		return client.get().uri(String.format("/books/%s", isbn))
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();
	}


}
