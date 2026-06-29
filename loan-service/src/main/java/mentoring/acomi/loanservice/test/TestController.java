package mentoring.acomi.loanservice.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {
	
	private final LoanViewRepository loanRepository;
	private final LoanEventRepository eventRepository;
	private final UserViewRepository userViewRepository;
	
	public TestController(LoanViewRepository loanRepository, LoanEventRepository eventRepository, UserViewRepository userViewRepository) {
        this.loanRepository = loanRepository;
        this.eventRepository = eventRepository;
        this.userViewRepository = userViewRepository;
    }

    @PostMapping("/reset")
    public void reset() {
    	loanRepository.deleteAll();
        eventRepository.deleteAll();
        userViewRepository.deleteAll();
    }

}
