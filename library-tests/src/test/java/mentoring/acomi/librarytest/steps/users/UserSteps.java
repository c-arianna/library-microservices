package mentoring.acomi.librarytest.steps.users;

import java.nio.charset.StandardCharsets;
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
import io.cucumber.java.en.When;
import mentoring.acomi.librarytest.config.TestConfig;
import mentoring.acomi.librarytest.steps.CommonSteps;
import mentoring.acomi.librarytest.support.Helper;
import mentoring.acomi.librarytest.support.TestContext;

public class UserSteps {
	

	
	private final TestContext context;

	private RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();

	public UserSteps(TestContext context) {
		this.context = context;
	}
	
	/*
	 * ############################### GIVEN #####################################
	 */
	
	@Given("l'utente {string} non è registrato")
	public void userNotSubscribed(String email) {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);

		var result = client.get().uri("/users").header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();
		
		Assertions.assertEquals(200, result.getStatus().value());
		
		String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
		var bodyResponse = JsonPath.parse(response);
		
		List<Map<String, Object>> items = bodyResponse.read("$.users");
		
		boolean found = items.size() > 0 && items.stream().anyMatch(u -> u.get("email").equals(email));
		
		Assertions.assertTrue(!found);
		
	}
	
	@Given("l'utente ha stato {string}")
	public void checkUserStatus(String status) {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		String loanId = context.get(CommonSteps.USER_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/users/%s".formatted(loanId))
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
	
	@Given("l'amministratore sospende l'utente")
	public void userSuspend() {
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		
		String userId = context.get(CommonSteps.USER_ID, String.class);
		
		String body = """
				{
				  "userId": "%s",
				  "reason": "Suspended"
				}
				""".formatted(userId);
		
		var result = suspendUser(body, accessToken);
		
		Assertions.assertEquals(200, result.getStatus().value());
	}
		
	/*
	 * ############################### WHEN #####################################
	 */
	
	@When("l'utente si registra con i seguenti dati:")
	public void userSubscribe(DocString body) {
		
		var result = client.post().uri("/users/subscribe").contentType(MediaType.APPLICATION_JSON).body(body.getContent()).exchange()
				.expectBody().returnResult();

		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
			
			if(result.getStatus().value() == 200) {
				var bodyResponse = JsonPath.parse(response);
				String userId = bodyResponse.read("$.userId");
	
				String userIdentityProviderId = bodyResponse.read("$.userIdentityProviderId");
			
				context.put(CommonSteps.USER_ID, userId);
				context.userProviderIdToDelete.add(userIdentityProviderId);
			}
				
		}
		
	}
	
	@When("l'utente si disiscrive con i seguenti dati:")
	public void userUnsubscribe(DocString body) {
	    String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		var result = client.post().uri("/users/unsubscribe").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body.getContent()).exchange()
				.expectBody().returnResult();
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}
	
	@When("l'amministratore sospende l'utente:")
	public void userSuspend(DocString body) {
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		String request = resolveDocString(body.getContent());
		var result = suspendUser(request, accessToken);
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}
	
	@When("l'amministratore riattiva l'utente:")
	public void userUnsuspend(DocString body) {
	    String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
	    String request = resolveDocString(body.getContent());
		var result = client.post().uri("/users/unsuspend").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(request).exchange()
				.expectBody().returnResult();
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}

	@When("l'utente visualizza il profilo di {string}")
	public void userGetUserProfile(String email) {
		
		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		
		String userId = context.get(email, String.class);
		
		if(userId == null) {
			throw new IllegalStateException("Missing field userId");
		}
		
		getUserProfile(accessToken, userId);
	
	}
	
	@When("l'amministratore visualizza il profilo di {string}")
	public void adminGetUserProfile(String email) {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		
		String userId = context.get(email, String.class);
		
		if(userId == null) {
		        throw new IllegalStateException("Missing field userId");
		}
		
		getUserProfile(accessToken, userId);
		
	}
	
	@When("l'amministratore visualizza il profilo dell'utente")
	public void adminGetUserProfile() {
		
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		
		String userId = context.get(CommonSteps.USER_ID, String.class);
		
		if(userId == null) {
		        throw new IllegalStateException("Missing field userId");
		}
		
		getUserProfile(accessToken, userId);
		
	}
	
	@When("l'amministratore visualizza l'elenco degli utenti")
	public void adminGetUsers() {
		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		getUsers(accessToken);		
	}
	
	@When("l'utente visualizza l'elenco degli utenti")
	public void userGetUsers() {
		String accessToken = context.get(CommonSteps.USER_ACCESS_TOKEN, String.class);
		getUsers(accessToken);		
	}
	
	/*
	 * ############################### THEN #####################################
	 */

	@Then("l'utente ha email {string}, ruolo {string}, stato {string}")
	public void checkUserView(String email, String role, String status) {

		String accessToken = context.get(CommonSteps.ADMIN_ACCESS_TOKEN, String.class);
		String userId = context.get(CommonSteps.USER_ID, String.class);

		Supplier<EntityExchangeResult<byte[]>> query = () -> client.get().uri("/users/%s".formatted(userId))
				.header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult();

		Helper.awaitAndAssert(query, json -> Assertions.assertAll(
			() -> {
				Assertions.assertEquals(200, query.get().getStatus().value());	
			},
			() -> {
				String responseEmail = json.read("$.email");
				Assertions.assertEquals(email, responseEmail);
			}, 
			() -> {
				String responseRole = json.read("$.role");
				Assertions.assertEquals(role, responseRole);
		    },
			() -> {
				String responseStatus = json.read("$.status");
				Assertions.assertEquals(status, responseStatus);
		}), 5000, 200);

	}

	/*
	 * ############################### HELPER METHODS  #####################################
	 */

	private EntityExchangeResult<byte[]> suspendUser(String body, String accessToken) {
		return client.post().uri("/users/suspend").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer %s".formatted(accessToken)).body(body).exchange()
				.expectBody().returnResult();
	}
	
	private void getUserProfile(String accessToken, String userId) {
		var result = client.get().uri("/users/%s".formatted(userId)).header("Authorization", "Bearer %s".formatted(accessToken))
				.exchange().expectBody().returnResult();
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}
	
	private void getUsers(String accessToken) {
		
		context.put(CommonSteps.LAST_QUERY,
				(Supplier<EntityExchangeResult<byte[]>>) () -> 
		            client.get().uri("/users").header("Authorization", "Bearer %s".formatted(accessToken)).exchange().expectBody().returnResult());

		Supplier<EntityExchangeResult<byte[]>> query = context.getTyped(CommonSteps.LAST_QUERY);
		var result = query.get();
		
		context.put(CommonSteps.RESPONSE_STATUS, result.getStatus().value());
		
		if (result.getResponseBody() != null) {
			String response = new String(result.getResponseBody(), StandardCharsets.UTF_8);
			context.put(CommonSteps.RESPONSE_BODY, response);
		}
	}
	
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
	
}
