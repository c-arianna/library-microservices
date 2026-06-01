package mentoring.acomi.userservice.infrastructure.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.userservice.application.services.UserService;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;

@RestController
public class UserController {

	private final UserService service;
	
	public UserController(UserService service) {
		this.service = service;
	}
	
	@PreAuthorize("hasAnyRole('READER')")
	@PostMapping("/unsubscribe")
	public UserResponse unsubscribe(@RequestBody @Valid UnsubscribeRequest request) {
		return service.unsubscribe(request);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/suspend")
	public UserResponse suspend(@RequestBody @Valid SuspendRequest request) {
		return service.suspend(request);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/unsuspend")
	public UserResponse unsuspend(@RequestBody @Valid SuspendRequest request) {
		return service.unsuspend(request);
	}
}
