package mentoring.acomi.userservice.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.sso.IdentityProviderService;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {
	
	private final UserViewRepository userRepository;
	private final UserEventRepository eventRepository;
	private final IdentityProviderService service;
	
	public TestController(UserViewRepository userRepository, UserEventRepository eventRepository, IdentityProviderService service) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.service = service;
    }

    @PostMapping("/reset")
    public void reset() {
    	userRepository.deleteAll();
        eventRepository.deleteAll();
    }
    
    @PostMapping("/reset/user/{userIdentityProviderId}")
    public void deleteUser(@PathVariable String userIdentityProviderId) {
    	service.deleteUser(userIdentityProviderId);
    }

}
