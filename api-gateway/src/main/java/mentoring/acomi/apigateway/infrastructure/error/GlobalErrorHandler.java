package mentoring.acomi.apigateway.infrastructure.error;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import mentoring.acomi.sharedlibrary.model.ErrorResponse;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler extends AbstractErrorWebExceptionHandler {

	public GlobalErrorHandler(ErrorAttributes errorAttributes, ApplicationContext applicationContext,
			ServerCodecConfigurer configurer) {
		super(errorAttributes, new WebProperties.Resources(), applicationContext);
		this.setMessageWriters(configurer.getWriters());
		this.setMessageReaders(configurer.getReaders());
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}

	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

		Throwable error = getError(request);

		if (error instanceof WebClientResponseException ex) {

			return ServerResponse.status(ex.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
					.bodyValue(ex.getResponseBodyAsString());
		}

		ErrorResponse response = mapGatewayError(error);
		
		return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).bodyValue(response);
	}

	private ErrorResponse mapGatewayError(Throwable error) {

		if (error instanceof IllegalArgumentException) {
			return new ErrorResponse("INVALID_REQUEST", error.getMessage(), "TECHNICAL", "api-gateway", Instant.now());
		}

		if (error instanceof TimeoutException) {
			return new ErrorResponse("TIMEOUT", "Downstream service timeout", "INFRASTRUCTURE", "api-gateway", Instant.now());
		}

		return new ErrorResponse("GATEWAY_ERROR", error.getMessage(), "TECHNICAL", "api-gateway", Instant.now());
	}
}
