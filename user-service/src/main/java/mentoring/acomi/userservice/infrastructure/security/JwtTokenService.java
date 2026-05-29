package mentoring.acomi.userservice.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import mentoring.acomi.userservice.application.security.JwtProperties;
import mentoring.acomi.userservice.application.security.TokenService;
import mentoring.acomi.userservice.domain.model.UserRole;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenService implements TokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    @Override
    public String generateAccessToken(String userId, String email, UserRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        
		claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "access");
                
        return createToken(claims, userId, expiry);
       
    }

    @Override
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.refreshTokenExpirationDays(), ChronoUnit.DAYS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        return createToken(claims, userId, expiry);
        
    }

    @Override
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public String extractSubject(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
    
    private String createToken(Map<String, Object> claims, String subject, Instant expiry) {
        
		Instant now = Instant.now();
		
        return Jwts.builder()
        		.issuer(jwtProperties.issuer())
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}