package mentoring.acomi.bookservice.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final String KEYCLOAK_FIELD_ROLES = "roles";
	private static final String KEYCLOAK_FIELD_REALM_ACCESS = "realm_access";
	private static final String KEYCLOAK_FIELD_USERNAME = "preferred_username";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {
		return http.csrf(csrf -> csrf.disable()).httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(form -> form.disable()).authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.build();
	}

	@Bean
	Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return jwt -> {
			Collection<GrantedAuthority> authorities = new ArrayList<>();

			Map<String, Object> realmAccess = jwt.getClaim(KEYCLOAK_FIELD_REALM_ACCESS);

			if (realmAccess != null) {
				Object roles = realmAccess.get(KEYCLOAK_FIELD_ROLES);
				if (roles instanceof Collection<?> roleCollection) {
					for (Object role : roleCollection) {
						String authority = role.toString();
						if (authority.startsWith("ROLE_")) {
							authorities.add(new SimpleGrantedAuthority(authority));
						} else {
							authorities.add(new SimpleGrantedAuthority(String.join("_", "ROLE", authority)));
						}
					}
				}
			}

			return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString(KEYCLOAK_FIELD_USERNAME));
		};
	}

}