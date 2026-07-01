package mentoring.acomi.apigateway.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
		return http.cors(withDefaults())
				.csrf(ServerHttpSecurity.CsrfSpec::disable).httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.exceptionHandling(
						ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
				.authorizeExchange(exchange -> {

					exchange.pathMatchers("/users/subscribe").permitAll();

					exchange.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
					
					if (testMode) {
						exchange.pathMatchers("/test/**").permitAll();
					}

					exchange.anyExchange().authenticated();

				}).oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults())).build();
	}
	
	@Bean
	CorsWebFilter corsWebFilter() {

	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);
	    config.addAllowedOrigin("http://localhost:4200");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("*");

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);

	    return new CorsWebFilter(source);
	}

}
