package mentoring.acomi.apigateway.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import mentoring.acomi.sharedlibrary.service.validator.TokenValidator;

@Configuration
public class SecurityConfig {

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(TokenValidator tokenValidator) {
		return new JwtAuthenticationFilter(tokenValidator);
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationEntryPoint entryPoint, AccessDeniedHandler accessDeniedHandler) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable).httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
				.authorizeExchange(exchange -> exchange.pathMatchers("/auth/**").permitAll().anyExchange().authenticated())
				.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION).build();
	}

}
