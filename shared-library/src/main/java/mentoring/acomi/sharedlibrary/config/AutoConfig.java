package mentoring.acomi.sharedlibrary.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedlibrary.security.JwtProperties;
import mentoring.acomi.sharedlibrary.service.JwtTokenService;
import mentoring.acomi.sharedlibrary.service.validator.TokenValidator;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "spring.security.jwt", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AutoConfig {

    @Bean
    JwtTokenService jwtTokenService(JwtProperties properties) {
        return new JwtTokenService(properties);
    }

    @Bean
    TokenValidator tokenValidator(JwtTokenService service) {
        return service;
    }

}