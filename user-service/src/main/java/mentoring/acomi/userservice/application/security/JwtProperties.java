package mentoring.acomi.userservice.application.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "spring.security.jwt")
@Validated
public record JwtProperties(
		@NotNull 
		String issuer,
		
		@NotNull 
		String secret,
        
		long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {}
