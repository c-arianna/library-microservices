package mentoring.acomi.userservice.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.sso.IdentityProviderService;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.OutboxJpaRepository;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {
	
	private final UserViewRepository userRepository;
	private final UserEventRepository eventRepository;
	private final OutboxJpaRepository outboxRepository;
	private final IdentityProviderService service;
	
	public TestController(UserViewRepository userRepository, UserEventRepository eventRepository, 
			IdentityProviderService service, OutboxJpaRepository outboxRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.outboxRepository = outboxRepository;
        this.service = service;
    }

    @PostMapping("/reset")
    public void reset() {
    	userRepository.deleteAllReaderUsers();
        eventRepository.deleteAll();
        outboxRepository.deleteAll();
    }
    
    @PostMapping("/reset/user/{userIdentityProviderId}")
    public void deleteUser(@PathVariable String userIdentityProviderId) {
    	userRepository.deleteByUserIdentityProviderId(userIdentityProviderId);
    	service.deleteUser(userIdentityProviderId);
    }

}
