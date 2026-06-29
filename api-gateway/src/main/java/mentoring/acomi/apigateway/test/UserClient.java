package mentoring.acomi.apigateway.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
@Profile("gherkin")
public class UserClient {

	private final WebClient webClient;

	public UserClient(WebClient.Builder builder, @Value("${user.service.url}") String baseUrl) {
		this.webClient = builder.baseUrl(baseUrl).build();
	}

	public Mono<Void> reset() {
		return webClient.post().uri("/test/reset").retrieve().bodyToMono(Void.class);
	}
	
	public Mono<Void> deleteUser(String userIdentityProviderId){
		return webClient.post().uri(String.format("/test/reset/user/%s", userIdentityProviderId)).retrieve().bodyToMono(Void.class);
	}

}