package mentoring.acomi.librarytest.steps.loans;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

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
import mentoring.acomi.librarytest.config.TestConfig;
import mentoring.acomi.librarytest.steps.CommonSteps;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class LoanSteps {

	private static final String USER_ID = "USER_ID";
	private static final String LOAN_ID = "LOAN_ID";
	
	private final TestContext context;

	private RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();

	public LoanSteps(TestContext context) {
		this.context = context;
	}

	/*
	 * ############################### GIVEN #####################################
	 */

	@Given("esiste l'utente con credenziali {string}, {string}")
	public void subscribeUser(String mail, String password) {

		String body = """
				{
				  "name": "Mario",
				  "lastname": "Rossi",
				  "email": "%s",
				  "password": "%s"
				}
				""".formatted(mail, password);

		var result = client.post().uri("/users/subscribe").contentType(MediaType.APPLICATION_JSON).body(body).exchange()
				.expectBody().returnResult();

		Assertions.assertEquals(200, result.getStatus().value());

		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);

		var bodyResponse = JsonPath.parse(response);

		String userId = bodyResponse.read("$.userId");
		String userIdentityProviderId = bodyResponse.read("$.userIdentityProviderId");

		context.put(USER_ID, userId);
		context.userPrividerIdToDelete.add(userIdentityProviderId);

	}

	@Given("il catalogo non contiene il libro con isbn {string}")
	public void bookNotExist(String isbn) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/books/%s".formatted(isbn))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Assertions.assertEquals(result.getStatus().value(), 404);
	}

	@Given("l'utente con ID {string} non esiste")
	public void userNotExist(String userId) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/users/%s".formatted(userId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Assertions.assertEquals(result.getStatus().value(), 404);
	}

	@Given("esiste un prestito dell'utente per il libro ISBN {string} in attesa di conferma")
	public void createLoan(String isbn) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		String userId = context.get("USER_ID", String.class);

		String body = """
				{
				  "isbn": "%s",
				  "userId": "%s",
				  "startDate": "2026-02-23"
				}
				""".formatted(isbn, userId);

		var result = client.post().uri("/loans").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange().expectBody()
				.returnResult();

		Assertions.assertEquals(201, result.getStatus().value());

		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);

		var bodyResponse = JsonPath.parse(response);
		String loanId = bodyResponse.read("$.loanId");

		context.put(LOAN_ID, loanId);

	}

	@Given("il prestito con ID {string} non esiste")
	public void loanNotExist(String loanId) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/loans/%s".formatted(loanId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Assertions.assertEquals(404, result.getStatus().value());

		context.put(LOAN_ID, loanId);

	}

	@Given("il prestito del libro è stato annullato")
	public void loanCanceled() {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String loanId = context.get("LOAN_ID", String.class);

		var result = client.post().uri("/loans/%s/reject".formatted(loanId)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();
		
		Assertions.assertEquals(204, result.getStatus().value());
		
	}
	
	@Given("il prestito del libro è stato confermato")
	public void loanConfirmed() {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String loanId = context.get("LOAN_ID", String.class);

		var result = client.post().uri("/loans/%s/confirm".formatted(loanId)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();
		
		Assertions.assertEquals(204, result.getStatus().value());
		
	}

	@Given("il prestito è in stato {string}")
	public void checkLoanStatus(String status) {
		
		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		String loanId = context.get(LOAN_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/loans/%s".formatted(loanId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(
			() -> {
				Assertions.assertEquals(200, query.get().getStatus().value());	
			}, 
			() -> {
				String responseStatus = json.read("$.status");
				Assertions.assertEquals(status, responseStatus);
		}), 5000, 200);
	}
	/*
	 * ############################### WHEN #####################################
	 */

	@When("l'utente crea una richiesta di prestito con i seguenti dati:")
	public void addLoan(DocString body) {
		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		addLoan(body, accessToken);
	}
	
	@When("l'amministratore crea una richiesta di prestito con i seguenti dati:")
	public void adminAddLoan(DocString body) {
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		addLoan(body, accessToken);
	}

	@When("l'amministratore conferma la richiesta del prestito")
	public void confimLoan() {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String loanId = context.get("LOAN_ID", String.class);

		var result = client.post().uri("/loans/%s/confirm".formatted(loanId)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
		
	}
	
	@When("l'amministratore annulla la richiesta del prestito")
	public void cancelLoan() {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String loanId = context.get("LOAN_ID", String.class);

		var result = client.post().uri("/loans/%s/reject".formatted(loanId)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}

	}
	
	@When("l'amministratore esegue l'operazione di reso del prestito")
	public void returnLoan() {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		String loanId = context.get("LOAN_ID", String.class);

		var result = client.post().uri("/loans/%s/return".formatted(loanId)).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).exchange().expectBody()
				.returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}
	
	@When("l'utente visualizza l'elenco dei prestiti")
	public void getLoans() {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		context.put(CommonSteps.LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/loans")
						.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
						.returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(CommonSteps.LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}
	
	@When("l'utente visualizza l'elenco dei prestiti, con filtro di ricerca")
	public void findLoansFilter(Map<String, String> rawFilters) {

		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);

		Map<String, String> filters = new LinkedHashMap<>();
		rawFilters.forEach((k, v) -> filters.put(k, Helper.normalize(v)));

		UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/loans");
		filters.forEach(uri::queryParam);

		context.put(CommonSteps.LAST_QUERY, (Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri(uri.build().toUri())
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(CommonSteps.LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));

	}
	
	@When("l'utente visualizza il dettaglio del prestito")
	public void getLoanDetail() {
		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
        getLoanDetail(accessToken);
	}
	
	@When("l'amministratore visualizza il dettaglio del prestito")
	public void getAdminLoanDetail() {
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
        getLoanDetail(accessToken);
	}
	/*
	 * ############################### THEN #####################################
	 */

	@Then("il prestito ha isbn {string}, userId {string}, stato {string}")
	public void checkLoanView(String isbn, String userId, String status) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		String loanId = context.get(LOAN_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/loans/%s".formatted(loanId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(
			() -> {
				Assertions.assertEquals(200, query.get().getStatus().value());	
			},
			() -> {
				String responseIsbn = json.read("$.isbn");
				Assertions.assertEquals(isbn, responseIsbn);
			}, () -> {
				String responseUserId = json.read("$.userId");
				String expectedUserId = Helper.resolve(userId, context);
				Assertions.assertEquals(expectedUserId, responseUserId);
			}, () -> {
				String responseStatus = json.read("$.status");
				Assertions.assertEquals(status, responseStatus);
		}), 5000, 200);

	}

	/*
	 * ############################### HELPER METHODS  #####################################
	 */

	private String resolveDocString(String docString) {

		if (docString == null) {
			return null;
		}

		String result = docString;

		for (String key : context.keys()) {
			String placeholder = String.format("${%s}", key);
			String value = context.get(key, String.class);

			if (value == null) {
				throw new AssertionError(String.format("Placeholder not resolved: ${%s}", key));
			}

			result = result.replace(placeholder, value);
		}

		return result;

	}
	
    private void getLoanDetail(String accessToken) {
		
		String loanId = context.get(LOAN_ID, String.class);
        
		context.put(CommonSteps.LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> client.get().uri("/loans/%s".formatted(loanId))
						.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody()
						.returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(CommonSteps.LAST_QUERY);
		var result = query.get();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		context.put(CommonSteps.RESPONSE_BODY, new String(result.getResponseBody(), StandardCharsets.UTF_8));
	}
    
    private void addLoan(DocString body, String accessToken) {
		String request = resolveDocString(body.getContent());

		var result = client.post().uri("/loans").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", String.join(" ", "Bearer", accessToken)).body(request).exchange().expectBody()
				.returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
			
			if(result.getStatus().value() == 201) {
				var bodyResponse = JsonPath.parse(response);
				String loanId = bodyResponse.read("$.loanId");
	
				context.put(LOAN_ID, loanId);
			}
				
		}
	}

}
