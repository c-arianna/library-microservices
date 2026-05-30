package mentoring.acomi.userservice.infrastructure.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.userservice.application.services.AuthService;
import mentoring.acomi.userservice.infrastructure.dto.AuthResponse;
import mentoring.acomi.userservice.infrastructure.dto.LoginRequest;
import mentoring.acomi.userservice.infrastructure.dto.LogoutRequest;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.TokenRefreshRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService service;

	public AuthController(AuthService service) {
		this.service = service;
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody @Valid LoginRequest request) {
		return service.login(request);
	}

	@PostMapping("/logout")
	public void logout(@RequestBody @Valid LogoutRequest request) {
		service.logout(request);
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@RequestBody TokenRefreshRequest request) {
		return service.refresh(request);
	}
	
	@PostMapping("/subscribe")
	public AuthResponse subscribe(@RequestBody @Valid SubscribeRequest request) {
		return service.subscribe(request);
	}
	

}
