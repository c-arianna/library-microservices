package mentoring.acomi.librarytest.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.web.servlet.client.RestTestClient;

import io.cucumber.java.After;
import mentoring.acomi.librarytest.config.TestConfig;

public class Hooks {

	private final RestTestClient client = RestTestClient.bindToServer().baseUrl(TestConfig.BASE_URL).build();;
	
	private final TestContext context;

	public Hooks(TestContext context) {
		this.context = context;
	}
	
	@After
	public void cleanup() {

	    List<Exception> errors = new ArrayList<>();

	    try {
	        client.post().uri("/test/reset").exchange().expectStatus().isOk().expectBody().isEmpty();
	    } catch (Exception ex) {
	        errors.add(ex);
	    }

	    List<String> usersToDelete = new ArrayList<>(context.userProviderIdToDelete);

	    for (String userIdentityProviderId : usersToDelete) {

	        try {
	            
	        	client.post().uri("/test/reset/user/%s".formatted(userIdentityProviderId))
	            	.exchange().expectStatus().isOk().expectBody().isEmpty();

	        } catch (Exception ex) {
	            errors.add(ex);
	        }
	    }

	    context.userProviderIdToDelete.clear();

	    if (!errors.isEmpty()) {
	        RuntimeException cleanupException = new RuntimeException("Errors occurred during test cleanup");
	        errors.forEach(cleanupException::addSuppressed);
	        throw cleanupException;
	    }
	}
	
}
