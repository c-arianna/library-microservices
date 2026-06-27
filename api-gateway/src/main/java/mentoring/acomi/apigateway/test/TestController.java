package mentoring.acomi.apigateway.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {

	private final BookClient bookClient;

	public TestController(BookClient bookClient) {
		this.bookClient = bookClient;
	}

	@PostMapping("/reset")
	public Mono<Void> resetAll() {
		return bookClient.reset();
	}
}
