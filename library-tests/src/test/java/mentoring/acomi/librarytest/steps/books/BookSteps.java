package mentoring.acomi.librarytest.steps.books;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mentoring.acomi.librarytest.clients.AuthClient;
import mentoring.acomi.librarytest.config.TestConfig;
import mentoring.acomi.librarytest.support.ExpectedValue;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class BookSteps {

	private static final String LAST_QUERY = "LAST_QUERY";
	private static final String KEYCLOAK_BASE_URL = TestConfig.KEYCLOAK_URL;
	private static final String CLIENT_ID = TestConfig.CLIENT_ID;
	private static final String KEYCLOAK_REALM = TestConfig.REALM;

	private static final String ADMIN_ACCESS_TOKEN = "adminAccessToken";
	private static final String USER_ACCESS_TOKEN = "userAccessToken";
	private static final String RESPONSE_BODY = "responseBody";
	private static final String RESPONSE_STATUS = "responseStatus";

	private final TestContext context;

	private RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();

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

		var result = client.post().uri("/books").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
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

		var result = client.post().uri("/books").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body.getContent()).exchange()
				.expectBody().returnResult();

		context.put(RESPONSE_STATUS, result.getStatus().value());
		context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
	}

	@When("l'utente visualizza il catalogo dei libri")
	public void getBooks() {

		String accessToken = context.get(USER_ACCESS_TOKEN, String.class);

		context.put(LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/books")
						.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
						.returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

		context.put(RESPONSE_STATUS, result.getStatus().value());
		context.put(RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}

	@When("l'utente visualizza il catalogo dei libri, con filtro di ricerca")
	public void findBooksFilter(Map<String, String> rawFilters) {

		String accessToken = context.get(USER_ACCESS_TOKEN, String.class);

		Map<String, String> filters = new LinkedHashMap<>();
		rawFilters.forEach((k, v) -> filters.put(k, Helper.normalize(v)));

		UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/books");
		filters.forEach(uri::queryParam);

		context.put(LAST_QUERY, (Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri(uri.build().toUri())
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

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

		context.put(LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/books/%s".formatted(isbn))
						.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
						.returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

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
		Object value = bodyResponse.read("$.%s".formatted(field));
		Assertions.assertNotNull(value, "Missing field: %s".formatted(field));
	}

	@Then("il libro {string} ha totalCopies = {int}, borrowedCopies = {int}, reservedCopies = {int}")
	public void checkBookView(String isbn, int totalCopies, int borrowedCopies, int reservedCopies) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);
		
		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
				.returnResult();

		awaitAndAssert(query,
			json -> Assertions.assertAll(() -> {
					Integer responseTotalCopies = json.read("$.totalCopies");
					Assertions.assertEquals(totalCopies, responseTotalCopies);
				}, () -> {
					Integer responseBorrowedCopies = json.read("$.borrowedCopies");
					Assertions.assertEquals(borrowedCopies, responseBorrowedCopies);
				}, () -> {
					Integer responseReservedCopies = json.read("$.reservedCopies");
					Assertions.assertEquals(reservedCopies, responseReservedCopies);
		}), 5000, 200);

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
			Assertions.assertTrue(expectedValue.matches(actualValue), String.format("Mismatch on field: %s, actual value: %s", key, actualValue));
		});
	}
	
	@Then("eventualmente la risposta contiene i seguenti campi:")
	public void checkResponseEventualContent(Map<String, String> expectedRaw) {
		
		Map<String, ExpectedValue> expectedContent = new LinkedHashMap<>();
		expectedRaw.forEach((k, v) -> {
			String resolved = Helper.resolve(v, context);
			expectedContent.put(k, Helper.normalizeExpected(resolved));
		});

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);

		if (query == null) {
			throw new IllegalStateException("No query found in context.");
		}

		awaitAndAssert(query,
				json -> {
					expectedContent.forEach((key, expectedValue) -> {
						Object actualValue = json.read("$.%s".formatted(key));
						Assertions.assertNotNull(actualValue, "Missing field in response: %s".formatted(key));
						Assertions.assertTrue(expectedValue.matches(actualValue), "Mismatch on field: %s, actual value: %s".formatted(key, actualValue));
					});

		        }, 5000, 200);
	}
		
	@Then("eventualmente {string} è una lista vuota")
	public void checkEmptyList(String field) {

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);

		if (query == null) {
			throw new IllegalStateException("No query found in context.");
		}

		awaitAndAssert(query,
				json -> {
						Integer size = json.read("$.%s.length()".formatted(field));
					    Assertions.assertEquals(0, size, "'%s' is not empty".formatted(field));
		        }, 5000, 200);
	}

	@Then("eventualmente {string} contiene {int} elementi")
	public void checkList(String field, int size) {

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);

		if (query == null) {
			throw new IllegalStateException("No query found in context.");
		}

		awaitAndAssert(query, json -> {
			Integer actual = json.read("$.%s.length()".formatted(field));
			Assertions.assertEquals(size, actual,
					"Expected %d elements in '%s', found %d".formatted(size, field, actual));
		}, 5000, 200);
	}

	@Then("eventualmente {string} ha un elemento con i campi:")
	public void checkContentList(String field, Map<String, String> expectedRaw) {

		Map<String, ExpectedValue> expected = new LinkedHashMap<>();
		expectedRaw.forEach((k, v) -> {
			String resolved = Helper.resolve(v, context);
			expected.put(k, Helper.normalizeExpected(resolved));
		});

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);

		if (query == null) {
			throw new IllegalStateException("No query found in context.");
		}

		awaitAndAssert(query, json -> {
			List<Map<String, Object>> items = json.read("$.%s".formatted(field));
			boolean found = items.stream().anyMatch(
					item -> expected.entrySet().stream().allMatch(e -> e.getValue().matches(item.get(e.getKey()))));
			Assertions.assertTrue(found);
		}, 5000, 200);
	}

	/*
	 * ############################### HELPER METHODS #####################################
	 */

	private String login(String username, String password) {

		AuthClient authClient = new AuthClient(KEYCLOAK_BASE_URL, KEYCLOAK_REALM);

		return authClient.login(CLIENT_ID, username, password);

	}

	private EntityExchangeResult<byte[]> addBookCopies(String isbn, String accessToken, String body) {
		return client.post().uri("/books/%s/copies/add".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}

	private EntityExchangeResult<byte[]> removeBookCopies(String isbn, String accessToken, String body) {
		return client.post().uri("/books/%s/copies/remove".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}

	private static void awaitAndAssert(Supplier<EntityExchangeResult<byte[]>> query, Consumer<DocumentContext> assertions, int timeoutMs, int intervalMs) {

		long start = System.currentTimeMillis();

		AssertionError lastError = null;

		while (System.currentTimeMillis() - start < timeoutMs) {

			try {

				var result = query.get();

				String body = new String(result.getResponseBody(), StandardCharsets.UTF_8);
				DocumentContext json = JsonPath.parse(body);

				assertions.accept(json);

				return;

			} catch (AssertionError e) {
				lastError = e;
			}

			try {
				Thread.sleep(intervalMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}

		if (lastError != null) {
			throw lastError;
		}

		throw new AssertionError("Condition not met within timeout");
	}

}
