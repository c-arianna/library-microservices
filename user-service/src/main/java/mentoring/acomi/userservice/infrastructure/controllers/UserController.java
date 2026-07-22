package mentoring.acomi.userservice.infrastructure.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.sharedcodelibrary.common.CardNumberUtils;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.UserFilter;
import mentoring.acomi.userservice.application.services.UserService;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserDetail;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;
import mentoring.acomi.userservice.infrastructure.dto.UserSubscribedResponse;
import mentoring.acomi.userservice.infrastructure.dto.UsersResponse;

@RestController
public class UserController {

	private final UserService service;
	
	public UserController(UserService service) {
		this.service = service;
	}
	
	@PreAuthorize("hasAnyRole('READER')")
	@PostMapping("/unsubscribe")
	public UserResponse unsubscribe(@RequestBody UnsubscribeRequest request) {
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
	
	@PostMapping("/subscribe")
	public UserSubscribedResponse subscribe(@RequestBody @Valid SubscribeRequest request) {
		return service.subscribe(request, "ROLE_READER");
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("/{userId}")
	public UserDetail getUserDetail(@PathVariable String userId) {
		return service.getUserDetail(userId);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("/")
	public UsersResponse getUsers(@RequestParam(required = false) String mail, @RequestParam(required = false) String name,
			@RequestParam(required = false) String lastname, @RequestParam(required = false) String cardNumber,
			@RequestParam(required = false) UserStatus status) {
		UserFilter filter = new UserFilter(mail, name, lastname, CardNumberUtils.normalizeCardNumber(cardNumber), status);
		return service.getUsers(filter);
	}
	
	@GetMapping("/profile")
	public UserDetail getUserProfile() {
		return service.getUserProfile();
	}
	
}
