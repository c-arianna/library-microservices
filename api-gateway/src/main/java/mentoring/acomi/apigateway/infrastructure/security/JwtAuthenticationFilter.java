package mentoring.acomi.apigateway.infrastructure.security;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import mentoring.acomi.sharedlibrary.model.TokenPrincipal;
import mentoring.acomi.sharedlibrary.service.validator.TokenValidator;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);

    private final TokenValidator tokenValidator;

    public JwtAuthenticationFilter(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    	
        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        	 return chain.filter(exchange);
        }

        String token = authorizationHeader.substring(7);

        try {
            
        	TokenPrincipal principal = tokenValidator.validateAndExtract(token);

            var authorities = List.of(new SimpleGrantedAuthority(String.join("_", "ROLE", principal.role().name())));

            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

            logger.debug("JWT authentication set for userId={}", principal.userId());

            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception ex) {
            logger.warn("Invalid JWT token", ex);
            return chain.filter(exchange);
        }
    }
}