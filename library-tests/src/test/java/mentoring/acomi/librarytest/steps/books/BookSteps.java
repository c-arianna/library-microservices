package mentoring.acomi.librarytest.steps.books;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mentoring.acomi.librarytest.config.TestConfig;
import mentoring.acomi.librarytest.steps.CommonSteps;
import mentoring.acomi.librarytest.support.ExpectedValue;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class BookSteps {

	private static final String LAST_QUERY = "LAST_QUERY";

	private static final String BOOK_REQUEST_ID = "BOOK_REQUEST_ID";

	private final TestContext context;

	private RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();

	public BookSteps(TestContext context) {
		this.context = context;
	}

	/*
	 * ############################### GIVEN #####################################
	 */

	@Given("l'utente si sottoscrive alla disponibilità del libro ISBN {string}")
	public void subscribeBookAvailability(String isbn) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		String body = """
				{
				  "phoneNumber": "+390000000000"
				}
				""";

		var result = client.post().uri("/books/%s/subscription".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();

		Assertions.assertEquals(204, result.getStatus().value());
	}

	@Given("esiste una richiesta per il libro isbn {string}, autore {string}, titolo {string}")
	public void createBookRequest(String isbn, String author, String title) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		String body = """
				{
				  "isbn": "%s",
				  "author": "%s",
				  "title": "%s",
				  "notes": ""
				}
				""".formatted(isbn, author, title);

		var result = client.post().uri("/books/requests").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();

		Assertions.assertEquals(201, result.getStatus().value());

		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);

		var bodyResponse = JsonPath.parse(response);
		String requestId = bodyResponse.read("$.requestId");

		context.put(BOOK_REQUEST_ID, requestId);

	}

	@Given("la richiesta è in stato {string}")
	public void checkBookRequestViewPreCondition(String status) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get()
				.uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> {
			String statusResponse = json.read("$.status");
			Assertions.assertEquals(status, statusResponse);
		}, 5000, 200);
	}

	@Given("la richiesta è stata rigettata")
	public void bookRequestRejected() {
		var result = rejectRequest();
		Assertions.assertEquals(204, result.getStatus().value());
	}

	@Given("la richiesta è stata approvata")
	public void bookRequestApproved() {
		var result = approveRequest();
		Assertions.assertEquals(204, result.getStatus().value());
	}

	@Given("la richiesta con ID {string} non esiste")
	public void bookRequestNotExist(String bookRequestId) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Assertions.assertEquals(404, result.getStatus().value());

		context.put(BOOK_REQUEST_ID, bookRequestId);

	}

	@Given("esiste un voto dell'utente per la richiesta")
	public void bookRequestVoted() {

		var result = voteRequest();
		Assertions.assertEquals(204, result.getStatus().value());

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get()
				.uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> {
			Integer votesResponse = json.read("$.votes");
			Assertions.assertEquals(2, votesResponse);
		}, 5000, 200);

	}

	@Given("la richiesta ha prezzo stimato {string} euro")
	public void assignEstimatedPrice(String price) {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		BigDecimal estimatedPrice = new BigDecimal(price);
		
		String body = """
				{
				   "estimatedPrice": %s
				}				
				""".formatted(estimatedPrice.toPlainString());
		
		var result = client.patch().uri("/books/requests/%s/estimatedPrice".formatted(bookRequestId))
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer %s".formatted(accessToken))
				.body(body).exchange().expectBody().returnResult();
		
		Assertions.assertEquals(204, result.getStatus().value());
		
		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get()
				.uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();
		
		Helper.awaitAndAssert(query, json -> Assertions.assertAll(() -> {
			String isbnResponse = json.read("$.requestId");
			Assertions.assertEquals(bookRequestId, isbnResponse);
		}, () -> {
			BigDecimal estimatedPriceResponse = json.read("$.estimatedPrice", BigDecimal.class);
			Assertions.assertTrue(estimatedPrice.compareTo(estimatedPriceResponse) == 0, 
					"Expected %s but was %s".formatted(price, estimatedPriceResponse));
		}), 5000, 200);
		
	}
	/*
	 * ############################### WHEN #####################################
	 */

	@When("l'amministratore aggiunge un libro al catalogo con i seguenti dati:")
	public void createBook(DocString body) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.post().uri("/books").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body.getContent()).exchange()
				.expectBody().returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
	}

	@When("l'utente visualizza il catalogo dei libri")
	public void getBooks() {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		context.put(LAST_QUERY, (Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/books")
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}

	@When("l'utente visualizza il catalogo dei libri, con filtro di ricerca")
	public void findBooksFilter(Map<String, String> rawFilters) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		Map<String, String> filters = new LinkedHashMap<>();
		rawFilters.forEach((k, v) -> filters.put(k, Helper.normalize(v)));

		UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/books");
		filters.forEach(uri::queryParam);

		context.put(LAST_QUERY, (Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri(uri.build().toUri())
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}

	@When("l'amministratore aggiunge una copia del libro {string}, con i seguenti dati:")
	public void addBookCopies(String isbn, DocString body) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = addBookCopies(isbn, accessToken, body.getContent());

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {
			context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
		}

	}

	@When("l'amministratore rimuove copie del libro {string}, con i seguenti dati:")
	public void removeBookCopies(String isbn, DocString body) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.post().uri("/books/%s/copies/remove".formatted(isbn))
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer %s".formatted(accessToken))
				.body(body.getContent()).exchange().expectBody().returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {
			context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
		}

	}

	@When("l'utente visualizza il dettaglio del libro isbn {string}")
	public void getBookDetail(String isbn) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		context.put(LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/books/%s".formatted(isbn))
						.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
						.returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}

	@When("l'utente crea una richiesta per un libro, con i seguenti dati:")
	public void createBookRequest(DocString body) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		var result = client.post().uri("/books/requests").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body.getContent()).exchange()
				.expectBody().returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {

			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);

			if (result.getStatus().value() == 201) {
				var bodyResponse = JsonPath.parse(response);
				String requestId = bodyResponse.read("$.requestId");

				context.put(BOOK_REQUEST_ID, requestId);
			}

		}

	}

	@When("l'utente visualizza le richieste libri")
	public void getBookRequests() {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		context.put(LAST_QUERY, (Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/books/requests")
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}

	@When("l'amministratore approva la richiesta")
	public void approveBookRequest() {

		var result = approveRequest();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}

	}

	@When("l'amministratore rigetta la richiesta")
	public void rejectBookRequest() {

		var result = rejectRequest();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}

	}

	@When("l'utente vota la richiesta")
	public void voteBookRequest() {

		var result = voteRequest();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}

	@When("l'utente visualizza il dettaglio della richiesta")
	public void getBookRequestDeatil() {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		context.put(LAST_QUERY, (Supplier<EntityExchangeResult<byte[]>>) () -> 
				client.get().uri("/books/requests/%s".formatted(bookRequestId)).header("Authorization", "Bearer %s".formatted(accessToken))
				.exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(LAST_QUERY);
		var result = query.get();
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());

		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}

	}
	
	@When("l'amministratore aggiorna il prezzo del libro richiesto con i seguenti dati:")
	public void updateBookRequestPrice(DocString body) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		var result = client.patch().uri("/books/requests/%s/estimatedPrice".formatted(bookRequestId))
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer %s".formatted(accessToken))
				.body(body.getContent()).exchange().expectBody().returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
		
	}
	
	@When("l'amministratore visuallizza le proposte di acquisto con budget {string} euro")
	public void getSuggestPurchaseBooks(String budget) {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
				
		BigDecimal budgetParam = new BigDecimal(budget);
		
		var result = client.get().uri("/books/requests/purchaseSuggestions?budget=%s".formatted(budgetParam))
			.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
		
	}

	/*
	 * ############################### THEN #####################################
	 */

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

		Helper.awaitAndAssert(query, json -> {
			expectedContent.forEach((key, expectedValue) -> {
				Object actualValue;

				try {
					actualValue = json.read("$.%s".formatted(key));
				} catch (PathNotFoundException e) {
					Assertions.fail("Missing field in response: %s".formatted(key));
					return;
				}

				Assertions.assertTrue(expectedValue.matches(actualValue), 
						"Mismatch on field: %s, actual value: %s".formatted(key, actualValue));
			});

		}, 5000, 200);
	}

	@Then("eventualmente la sottoscrizione con ISBN {string} risulta notificata")
	public void checkSubscriptionStatus(String isbn) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		Supplier<EntityExchangeResult<byte[]>> isbnSubscriptions = () -> client.get()
				.uri("/books/%s/subscriptions".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(isbnSubscriptions, json -> Assertions.assertAll(() -> {
			List<String> statuses = json.read("$.subscriptions[*].status");
			Assertions.assertFalse(statuses.isEmpty());
			Assertions.assertTrue(statuses.stream().allMatch("NOTIFIED"::equals));

		}), 5000, 200);
	}

	@Then("la richiesta ha ISBN {string}, autore {string}, titolo {string}, stato {string}, voti {int}")
	public void checkBookRequestView(String isbn, String author, String title, String status, int votes) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get()
				.uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(() -> {
			String isbnResponse = json.read("$.requestId");
			Assertions.assertEquals(bookRequestId, isbnResponse);
		}, () -> {
			String authorResponse = json.read("$.author");
			Assertions.assertEquals(author, authorResponse);
		}, () -> {
			String titleResponse = json.read("$.title");
			Assertions.assertEquals(title, titleResponse);
		}, () -> {
			String statusResponse = json.read("$.status");
			Assertions.assertEquals(status, statusResponse);
		}, () -> {
			Integer votesResponse = json.read("$.votes");
			Assertions.assertEquals(votes, votesResponse);
		}), 5000, 200);
	}

	@Then("la lista dei voti della richiesta contiene {int} elementi")
	public void checkVoteList(int size) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get()
				.uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> {
			Integer actual = json.read("$.bookRequestVotes.length()");
			Assertions.assertEquals(size, actual, "Expected %d elements', found %d".formatted(size, actual));
		}, 5000, 200);

	}

	@Then("la richiesta ha ISBN {string}, prezzo stimato {string}")
	public void checkBookRequestViewPrice(String isbn, String estimatedPrice) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get()
				.uri("/books/requests/%s".formatted(bookRequestId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		BigDecimal price = new BigDecimal(estimatedPrice);
		
		Helper.awaitAndAssert(query, json -> Assertions.assertAll(() -> {
			String isbnResponse = json.read("$.requestId");
			Assertions.assertEquals(bookRequestId, isbnResponse);
		}, () -> {
			BigDecimal estimatedPriceResponse = json.read("$.estimatedPrice", BigDecimal.class);
			Assertions.assertEquals(price, estimatedPriceResponse);
		}), 5000, 200);
	}
	
	@Then("la lista dei libri suggeriti contiene {int} elementi")
	public void checkPurchaseSuggestBookList(int size) {

		String body = context.get(CommonSteps.RESPONSE_BODY, String.class);
		var responseBody = JsonPath.parse(body);
		
		Integer actual = responseBody.read("$.books.length()");
		Assertions.assertEquals(size, actual, "Expected %d elements', found %d".formatted(size, actual));
		
	}
	
	@Then("la lista dei libri suggeriti ha un elemento con i campi:")
    public void checkPurchaseSuggestBookListContent(Map<String, String> expectedRaw) {
		
		Map<String, ExpectedValue> expected = new LinkedHashMap<>();
		expectedRaw.forEach((k, v) -> {
			String resolved = Helper.resolve(v, context);
			expected.put(k, Helper.normalizeExpected(resolved));
		});

		String body = context.get(CommonSteps.RESPONSE_BODY, String.class);

		var bodyResponse = JsonPath.parse(body);
		
		List<Map<String, Object>> items = bodyResponse.read("$.books");
		
		boolean found = items.stream().anyMatch(item -> matchesExpectedFields(item, expected));
		
		Assertions.assertTrue(found, "Nessun elemento della lista contiene i campi attesi: %s, response: ".formatted(expected, body));
	}
	
	/*
	 * ############################### HELPER METHODS #####################################
	 */

	private EntityExchangeResult<byte[]> addBookCopies(String isbn, String accessToken, String body) {
		return client.post().uri("/books/%s/copies/add".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}

	private EntityExchangeResult<byte[]> approveRequest() {
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		return client.post().uri("/books/requests/%s/approve".formatted(bookRequestId))
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();

	}

	private EntityExchangeResult<byte[]> rejectRequest() {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		String body = """
				{
				   "reason": "Il libro non è più disponibile per l'acquisto"
				}
				   """;

		return client.post().uri("/books/requests/%s/reject".formatted(bookRequestId))
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).body(body).exchange().expectBody()
				.returnResult();

	}

	private EntityExchangeResult<byte[]> voteRequest() {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		String bookRequestId = context.get(BOOK_REQUEST_ID, String.class);

		return client.post().uri("/books/requests/%s/vote".formatted(bookRequestId))
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();

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
