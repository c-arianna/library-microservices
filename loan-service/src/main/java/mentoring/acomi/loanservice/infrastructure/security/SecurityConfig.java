package mentoring.acomi.loanservice.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayAuthenticationFilter gatewayAuthenticationFilter)
			throws Exception {
		return http.csrf(csrf -> csrf.disable()).httpBasic(httpBasic -> httpBasic.disable()).formLogin(form -> form.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	GatewayAuthenticationFilter gatewayAuthenticationFilter() {
		return new GatewayAuthenticationFilter();
	}

}