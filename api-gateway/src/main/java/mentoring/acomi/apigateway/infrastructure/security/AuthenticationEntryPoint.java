package mentoring.acomi.apigateway.infrastructure.security;

import java.time.Instant;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import mentoring.acomi.sharedlibrary.model.ErrorResponse;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public AuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {

		var response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		ErrorResponse errorResponse = new ErrorResponse("AUTHENTICATION_ERROR",
				"Authentication required or invalid token", "SECURITY", "api-gateway", Instant.now());

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
			DataBuffer buffer = response.bufferFactory().wrap(bytes);
			return response.writeWith(Mono.just(buffer));
		} catch (JacksonException e) {
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return response.setComplete();
		}

	}
}
