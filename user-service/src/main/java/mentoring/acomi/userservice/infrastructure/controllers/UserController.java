package mentoring.acomi.userservice.infrastructure.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.userservice.application.services.UserService;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeResponse;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService service;
	
	public UserController(UserService service) {
		this.service = service;
	}
	
	@PostMapping("/subscribe")
	public SubscribeResponse subscribe(@RequestBody @Valid SubscribeRequest request) {
		return service.subscribe(request);
	}
	
	@PostMapping("/unsubscribe")
	public UserResponse unsubscribe(@RequestBody @Valid UnsubscribeRequest request) {
		return service.unsubscribe(request);
	}
	
	@PostMapping("/suspend")
	public UserResponse suspend(@RequestBody @Valid SuspendRequest request) {
		return service.suspend(request);
	}
	
	@PostMapping("/unsuspend")
	public UserResponse unsuspend(@RequestBody @Valid SuspendRequest request) {
		return service.unsuspend(request);
	}
}
