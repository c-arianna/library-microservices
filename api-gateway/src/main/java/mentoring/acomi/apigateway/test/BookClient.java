package mentoring.acomi.apigateway.test;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class BookClient {

	private final WebClient webClient;

	public BookClient(WebClient.Builder builder) {
		this.webClient = builder.baseUrl("http://book-service:8080").build();
	}

	public Mono<Void> reset() {
		return webClient.post().uri("/test/reset").retrieve().bodyToMono(Void.class);
	}

}
