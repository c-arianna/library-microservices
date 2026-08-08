package mentoring.acomi.librarytest.steps;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.jayway.jsonpath.JsonPath;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import mentoring.acomi.librarytest.clients.AuthClient;
import mentoring.acomi.librarytest.config.TestConfig;
import mentoring.acomi.librarytest.support.ExpectedValue;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class CommonSteps {

	private final TestContext context;

	private static final String KEYCLOAK_BASE_URL = "http://localhost:8084";
	private static final String CLIENT_ID = "library-test-client";
	private static final String KEYCLOAK_REALM = "library-microservices-test";

	public static final String ADMIN_ACCESS_TOKEN = "adminAccessToken";
	public static final String USER_ACCESS_TOKEN = "userAccessToken";
	public static final String RESPONSE_BODY = "responseBody";
	public static final String RESPONSE_STATUS = "responseStatus";
	public static final String LAST_QUERY = "LAST_QUERY";

	public static final String USER_ID = "USER_ID";
	public static final String CARD_NUMBER = "CARD_NUMBER";

	private RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();;

	public CommonSteps(TestContext context) {
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

		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);

		var bodyResponse = JsonPath.parse(response);

		String isbnResponse = bodyResponse.read("$.isbn");

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/books/%s".formatted(isbnResponse))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(() -> {
			Assertions.assertEquals(200, query.get().getStatus().value());
		}), 5000, 200);

	}

	@Given("l'amministratore aggiunge {int} copie del libro {string}")
	public void addBookCopies(int quantity, String isbn) {

		String accessToken = context.get(ADMIN_ACCESS_TOKEN, String.class);

		int initialTotalCopies = getInitialTotalCopies(isbn, accessToken);

		String body = """
				{
				  "quantity": %d
				}
				""".formatted(quantity);

		var result = addBookCopies(isbn, accessToken, body);

		Assertions.assertEquals(204, result.getStatus().value());

		Supplier<EntityExchangeResult<byte[]>> detail = () -> client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(detail, json -> Assertions.assertAll(() -> {
			Integer responseTotalCopies = json.read("$.totalCopies");
			Assertions.assertEquals(initialTotalCopies + quantity, responseTotalCopies);
		}), 5000, 200);

	}

	@Given("l'amministratore rimuove una copia del libro {string}")
	public void removeCopies(String isbn) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		int initialTotalCopies = getInitialTotalCopies(isbn, accessToken);
		
		String body = """
				{
				  "quantity": 1,
				  "reason": "Book Lost"
				}
				""";

		var result = removeBookCopies(isbn, accessToken, body);

		Assertions.assertEquals(204, result.getStatus().value());

		Supplier<EntityExchangeResult<byte[]>> detail = () -> client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(detail, json -> Assertions.assertAll(() -> {
			Integer responseTotalCopies = json.read("$.totalCopies");
			Assertions.assertEquals(initialTotalCopies - 1, responseTotalCopies);
		}), 5000, 200);
	}

	@Given("esiste l'utente con credenziali {string}, {string}, nome {string}, cognome {string}")
	public void subscribeUser(String mail, String password, String name, String lastname) {

		String body = """
				{
				  "name": "%s",
				  "lastname": "%s",
				  "email": "%s",
				  "password": "%s"
				}
				""".formatted(name, lastname, mail, password);

		var result = client.post().uri("/users/subscribe").contentType(MediaType.APPLICATION_JSON).body(body).exchange()
				.expectBody().returnResult();

		Assertions.assertEquals(200, result.getStatus().value());

		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);

		var bodyResponse = JsonPath.parse(response);

		String userId = bodyResponse.read("$.userId");
		String userIdentityProviderId = bodyResponse.read("$.userIdentityProviderId");
		String cardNumber = bodyResponse.read("$.cardNumber");

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/users/%s".formatted(userId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(() -> {
			Assertions.assertEquals(200, query.get().getStatus().value());
		}, () -> {
			String responseCardNumber = json.read("$.cardNumber");
			Assertions.assertEquals(cardNumber, responseCardNumber);
		}), 5000, 200);

		context.put(USER_ID, userId);
		context.put(CARD_NUMBER, cardNumber);
		context.userProviderIdToDelete.add(userIdentityProviderId);
		context.put(mail, userId);

	}

	@Given("l'utente con ID {string} non esiste")
	public void userNotExist(String userId) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/users/%s".formatted(userId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Assertions.assertEquals(result.getStatus().value(), 404);

		context.put(USER_ID, userId);
	}
	
	@Given("il catalogo non contiene il libro con isbn {string}")
	public void bookNotExist(String isbn) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Assertions.assertEquals(result.getStatus().value(), 404);
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

	@Then("la risposta contiene i seguenti campi:")
	public void checkResponseContent(Map<String, String> expectedRaw) {

		String body = context.get(CommonSteps.RESPONSE_BODY, String.class);
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

	@Then("eventualmente {string} è una lista vuota")
	public void checkEmptyList(String field) {

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);

		if (query == null) {
			throw new IllegalStateException("No query found in context.");
		}

		Helper.awaitAndAssert(query, json -> {
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

		Helper.awaitAndAssert(query, json -> {
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

		Helper.awaitAndAssert(query, json -> {
			List<Map<String, Object>> items = json.read("$.%s".formatted(field));
			boolean found = items.stream().anyMatch(
					item -> expected.entrySet().stream().allMatch(e -> e.getValue().matches(item.get(e.getKey()))));
			Assertions.assertTrue(found);
		}, 5000, 200);
	}

	@Then("il libro {string} ha totalCopies = {int}, borrowedCopies = {int}, reservedCopies = {int}")
	public void checkBookView(String isbn, int totalCopies, int borrowedCopies, int reservedCopies) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(() -> {
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

	@Then("la risposta contiene {int} elementi")
	public void responseListNotEmpty(int size) {
		String body = context.get(RESPONSE_BODY, String.class);
		var bodyResponse = JsonPath.parse(body);
		List<Map<String, Object>> items = bodyResponse.read("$[*]");
		Assertions.assertEquals(size, items.size(), "Expected %d elements in response', found %d".formatted(size, items.size()));
	}
	
	@Then("la risposta contiene un elemento con i campi:")
	public void checkContentResponseList(Map<String, String> expectedRaw) {

		Map<String, ExpectedValue> expected = new LinkedHashMap<>();
		expectedRaw.forEach((k, v) -> {
			String resolved = Helper.resolve(v, context);
			expected.put(k, Helper.normalizeExpected(resolved));
		});

		String body = context.get(RESPONSE_BODY, String.class);

		var bodyResponse = JsonPath.parse(body);
		
		List<Map<String, Object>> items = bodyResponse.read("$[*]");
		
		
		boolean found = items.stream().anyMatch(item -> matchesExpectedFields(item, expected));
		
		Assertions.assertTrue(found, "Nessun elemento della response contiene i campi attesi: %s, response: ".formatted(expected, body));
		
	}
	
	/*
	 * ############################### HELPER METHODS #####################################
	 */

	private String login(String username, String password) {

		AuthClient authClient = new AuthClient(KEYCLOAK_BASE_URL, KEYCLOAK_REALM);

		return authClient.login(CLIENT_ID, username, password);

	}

	private EntityExchangeResult<byte[]> addBookCopies(String isbn, String accessToken, String body) {

		return client.post().uri(String.format("/books/%s/copies/add", isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}

	public EntityExchangeResult<byte[]> removeBookCopies(String isbn, String accessToken, String body) {
		return client.post().uri("/books/%s/copies/remove".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}
	
	private int getInitialTotalCopies(String isbn, String accessToken) {
		
		Supplier<EntityExchangeResult<byte[]>> detail = () -> client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		var initialResponse = new String(detail.get().getResponseBody(), StandardCharsets.UTF_8);

		var initialJson = JsonPath.parse(initialResponse);

		return initialJson.read("$.totalCopies");
	}
	
	private boolean matchesExpectedFields(Map<String, Object> item, Map<String, ExpectedValue> expected) {

	    return expected.entrySet().stream().allMatch(entry -> {

	                String fieldName = entry.getKey();
	                ExpectedValue expectedValue = entry.getValue();

	                if (!item.containsKey(fieldName)) {
	                    return false;
	                }

	                Object actualValue = item.get(fieldName);

	                return expectedValue.matches(actualValue);
	            });
	}
	

}
