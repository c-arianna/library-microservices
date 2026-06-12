package mentoring.acomi.loanservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            return Jwt.withTokenValue("test")
                .header("alg", "none")
                .claim("sub", "test-user")
                .build();
        };
    }
}

