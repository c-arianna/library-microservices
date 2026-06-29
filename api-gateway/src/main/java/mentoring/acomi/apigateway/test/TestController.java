package mentoring.acomi.apigateway.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {

	private final BookClient bookClient;
	private final UserClient userClient;
	private final LoanClient loanClient;
	
	public TestController(BookClient bookClient, UserClient userClient, LoanClient loanClient) {
		this.bookClient = bookClient;
		this.loanClient = loanClient;
		this.userClient = userClient;
	}

	@PostMapping("/reset")
	public Mono<Void> resetAll() {
		return Mono.when(bookClient.reset(), loanClient.reset(), userClient.reset());
	}
	
	@PostMapping("/reset/user/{userIdentityProviderId}")
	public Mono<Void> deleteUser(@PathVariable String userIdentityProviderId){
		return Mono.when(userClient.deleteUser(userIdentityProviderId));
	}
}
