package mentoring.acomi.loanservice.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {
	
	private final LoanViewRepository loanRepository;
	private final LoanEventRepository eventRepository;
	private final UserViewRepository userViewRepository;
	private final OutboxRepository outboxRepository;
	private final UserLoanStatisticRepository userLoanStatisticRepository;
	private final DailyLoanStatisticRepository dailyStatisticRepository;
	private final BookViewRepository bookRepository;
	private final PopularBookViewRepository popularBookRepository;
	
	public TestController(LoanViewRepository loanRepository, LoanEventRepository eventRepository, UserViewRepository userViewRepository,
			OutboxRepository outboxRepository, UserLoanStatisticRepository userLoanStatisticRepository, 
			DailyLoanStatisticRepository dailyStatisticRepository, BookViewRepository bookRepository,
			PopularBookViewRepository popularBookRepository) {
        this.loanRepository = loanRepository;
        this.eventRepository = eventRepository;
        this.userViewRepository = userViewRepository;
        this.outboxRepository = outboxRepository;
        this.userLoanStatisticRepository = userLoanStatisticRepository;
        this.dailyStatisticRepository = dailyStatisticRepository;
        this.bookRepository = bookRepository;
        this.popularBookRepository = popularBookRepository;
    }

    @PostMapping("/reset")
    public void reset() {
    	loanRepository.deleteAll();
        eventRepository.deleteAll();
        userViewRepository.deleteAll();
        outboxRepository.deleteAll();
        userLoanStatisticRepository.deleteAll();
        dailyStatisticRepository.deleteAll();
        bookRepository.deleteAll();
        popularBookRepository.deleteAll();
    }

}
