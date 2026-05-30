package mentoring.acomi.userservice.application.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedlibrary.security.JwtProperties;
import mentoring.acomi.sharedlibrary.service.generator.TokenGenerator;
import mentoring.acomi.userservice.application.errors.InvalidLogin;
import mentoring.acomi.userservice.application.errors.InvalidRefreshToken;
import mentoring.acomi.userservice.application.errors.InvalidUserStatus;
import mentoring.acomi.userservice.application.repositories.RefreshTokenRepository;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.security.RefreshTokenGenerator;
import mentoring.acomi.userservice.application.view.RefreshToken;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.errors.UserNotExist;
import mentoring.acomi.userservice.domain.model.User;
import mentoring.acomi.userservice.domain.model.UserStatus;
import mentoring.acomi.userservice.infrastructure.dto.AuthResponse;
import mentoring.acomi.userservice.infrastructure.dto.LoginRequest;
import mentoring.acomi.userservice.infrastructure.dto.LogoutRequest;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.TokenRefreshRequest;

import java.time.LocalDateTime;

@Service
public class AuthService {

	private final UserViewRepository userViewRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenGenerator tokenService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProperties jwtProperties;
	private final RefreshTokenGenerator refreshTokenGenerator;
    private final UserService userService;
    
	public AuthService(UserViewRepository userViewRepository, PasswordEncoder passwordEncoder,
			TokenGenerator tokenService, RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties,
			RefreshTokenGenerator refreshTokenGenerator, UserService userService) {
		this.userViewRepository = userViewRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtProperties = jwtProperties;
		this.refreshTokenGenerator = refreshTokenGenerator;
		this.userService = userService;
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {

		String email = request.email();
		UserView userView = userViewRepository.findByEmail(email)
				.orElseThrow(() -> InvalidLogin.invalidCredentials(request.email()));

		if (!passwordEncoder.matches(request.password(), userView.passwordHash())) {
			throw InvalidLogin.invalidCredentials(email);
		}

		if (UserStatus.SUSPENDED.equals(userView.status())) {
			throw InvalidLogin.userSuspended(email);
		}

		if (UserStatus.DISABLE.equals(userView.status())) {
			throw InvalidLogin.userDisabled(email);
		}

		String accessToken = tokenService.generateAccessToken(userView.id(), userView.email(), userView.role());

		String refreshToken = refreshTokenGenerator.generate();

		LocalDateTime refreshExpiry = LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpirationDays());

		refreshTokenRepository.save(new RefreshToken(userView.id(), refreshToken, refreshExpiry, false));

		return new AuthResponse(userView.id(), accessToken, refreshToken);
	}

	@Transactional
	public void logout(LogoutRequest request) {
		refreshTokenRepository.revoke(request.refreshToken());
	}

	@Transactional
	public AuthResponse refresh(TokenRefreshRequest request) {

		String refreshTokenRequest = request.refreshToken();

		RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshTokenRequest).orElseThrow(
				() -> new InvalidRefreshToken(String.format("Refresh token not found, %s", refreshTokenRequest)));

		if (storedRefreshToken.revoked()) {
			throw new InvalidRefreshToken(
					String.format("Refresh token %s already used or revoked", refreshTokenRequest));
		}

		if (storedRefreshToken.expiresAt().isBefore(LocalDateTime.now())) {
			refreshTokenRepository.revoke(refreshTokenRequest);
			throw new InvalidRefreshToken(String.format("Refresh token %s expired", refreshTokenRequest));
		}

		String userId = storedRefreshToken.userId();
		UserView user = userViewRepository.findById(userId)
				.orElseThrow(() -> new UserNotExist(String.format("User not found, %s, ", userId)));

		if (UserStatus.SUSPENDED.equals(user.status())) {
			throw new InvalidUserStatus(String.format("User %s is suspended", userId));
		}

		if (UserStatus.DISABLE.equals(user.status())) {
			throw new InvalidUserStatus(String.format("User %s is unsubscribed", userId));
		}
		
		refreshTokenRepository.revoke(refreshTokenRequest);

		String newAccessToken = tokenService.generateAccessToken(user.id(), user.email(), user.role());
		String newRefreshToken = refreshTokenGenerator.generate();

		LocalDateTime newExpiry  = LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpirationDays());

		refreshTokenRepository.save(new RefreshToken(user.id(), newRefreshToken, newExpiry , false));

		return new AuthResponse(user.id(), newAccessToken, newRefreshToken);
	}

	public AuthResponse subscribe(SubscribeRequest request) {
		
		User user = userService.subscribe(request);
		String userId = user.getId();
		
		String accessToken = tokenService.generateAccessToken(userId, user.getEmail().getValue(), user.getRole());
		String refreshToken = refreshTokenGenerator.generate();
		
		LocalDateTime newExpiry  = LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpirationDays());

		refreshTokenRepository.save(new RefreshToken(userId, refreshToken, newExpiry , false));
		
		return new AuthResponse(userId, accessToken, refreshToken);
	}

}
