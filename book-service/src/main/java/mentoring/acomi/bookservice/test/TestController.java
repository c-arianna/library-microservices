package mentoring.acomi.bookservice.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.repositories.UserViewRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {

    private final BookViewRepository bookRepository;
    private final BookEventRepository eventRepository;
    private final UserViewRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final BookRequestViewRepository bookRequestRepository;
    private final BookRequestVoteViewRepository bookRequestVoteRepository;

    public TestController(BookViewRepository bookRepository, BookEventRepository eventRepository, OutboxRepository outboxRepository,
    		UserViewRepository userRepository, BookRequestViewRepository bookRequestRepository,
    		BookRequestVoteViewRepository bookRequestVoteRepository) {
        this.bookRepository = bookRepository;
        this.eventRepository = eventRepository;
        this.outboxRepository = outboxRepository;
        this.userRepository = userRepository;
        this.bookRequestRepository = bookRequestRepository;
        this.bookRequestVoteRepository = bookRequestVoteRepository;
    }

    @PostMapping("/reset")
    public void reset() {
        bookRepository.deleteAll();
        eventRepository.deleteAll();
        outboxRepository.deleteAll();
        userRepository.deleteAll();
        bookRequestRepository.deleteAll();
        bookRequestVoteRepository.deleteAll();
    }
}

