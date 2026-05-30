package mentoring.acomi.sharedlibrary.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import mentoring.acomi.sharedlibrary.model.TokenPrincipal;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.security.JwtProperties;
import mentoring.acomi.sharedlibrary.service.generator.TokenGenerator;
import mentoring.acomi.sharedlibrary.service.validator.TokenValidator;

import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class JwtTokenService implements TokenGenerator, TokenValidator {

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

		return Jwts.builder().issuer(jwtProperties.issuer()).subject(userId).claim("role", role.name())
				.issuedAt(Date.from(now)).expiration(Date.from(expiry)).id(UUID.randomUUID().toString())
				.signWith(signingKey, Jwts.SIG.HS256).compact();

	}

	@Override
	public TokenPrincipal validateAndExtract(String token) {

		Claims claims = parseClaims(token);
		String userId = claims.getSubject();
		String roleName = claims.get("role", String.class);

		if (ObjectUtils.isEmpty(userId)) {
			throw new JwtException("Missing subject");
		}

		if (ObjectUtils.isEmpty(roleName)) {
			throw new JwtException("Missing subject");
		}

		UserRole role;
		try {
			role = UserRole.valueOf(roleName);
		} catch (IllegalArgumentException ex) {
			throw new JwtException("Invalid role claim", ex);
		}

		return new TokenPrincipal(userId, role);
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(signingKey).requireIssuer(jwtProperties.issuer()).build()
				.parseSignedClaims(token).getPayload();
	}

}