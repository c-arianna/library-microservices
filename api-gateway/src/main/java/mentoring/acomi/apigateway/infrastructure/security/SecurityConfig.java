package mentoring.acomi.apigateway.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Value("${app.test-mode:false}")
	private boolean testMode;

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationEntryPoint entryPoint,
			AccessDeniedHandler accessDeniedHandler) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable).httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.exceptionHandling(
						ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
				.authorizeExchange(exchange -> {

					exchange.pathMatchers("/users/subscribe").permitAll();

					if (testMode) {
						exchange.pathMatchers("/test/**").permitAll();
					}

					exchange.anyExchange().authenticated();

				}).oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults())).build();
	}

}
