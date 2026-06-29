package mentoring.acomi.apigateway.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
@Profile("gherkin")
public class LoanClient {

	private final WebClient webClient;

	public LoanClient(WebClient.Builder builder, @Value("${loan.service.url}") String baseUrl) {
		this.webClient = builder.baseUrl(baseUrl).build();
	}

	public Mono<Void> reset() {
		return webClient.post().uri("/test/reset").retrieve().bodyToMono(Void.class);
	}

}