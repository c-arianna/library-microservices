package mentoring.acomi.librarytest.steps.books;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mentoring.acomi.librarytest.config.TestConfig;
import mentoring.acomi.librarytest.steps.CommonSteps;
import mentoring.acomi.librarytest.support.ExpectedValue;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class BookSteps {

	private static final String LAST_QUERY = "LAST_QUERY";
	
	private final TestContext context;

	private RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();

	public BookSteps(TestContext context) {
		this.context = context;
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

		context.put(LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/books")
						.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
						.returnResult());

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

		var result = client.post().uri("/books/%s/copies/remove".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body.getContent()).exchange().expectBody()
				.returnResult();

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

		Helper.awaitAndAssert(query,
				json -> {
					expectedContent.forEach((key, expectedValue) -> {
						Object actualValue = json.read("$.%s".formatted(key));
						Assertions.assertNotNull(actualValue, "Missing field in response: %s".formatted(key));
						Assertions.assertTrue(expectedValue.matches(actualValue), "Mismatch on field: %s, actual value: %s".formatted(key, actualValue));
					});

		        }, 5000, 200);
	}
		
	/*
	 * ############################### HELPER METHODS #####################################
	 */

	private EntityExchangeResult<byte[]> addBookCopies(String isbn, String accessToken, String body) {
		return client.post().uri("/books/%s/copies/add".formatted(isbn)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();
	}

}
