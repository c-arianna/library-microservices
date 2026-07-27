package mentoring.acomi.bookservice.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;

@RestController
@RequestMapping("/test")
@Profile("gherkin")
public class TestController {

    private final BookViewRepository bookRepository;
    private final BookEventRepository eventRepository;
    private final OutboxRepository outboxRepository;

    public TestController(BookViewRepository bookRepository, BookEventRepository eventRepository, OutboxRepository outboxRepository) {
        this.bookRepository = bookRepository;
        this.eventRepository = eventRepository;
        this.outboxRepository = outboxRepository;
    }

    @PostMapping("/reset")
    public void reset() {
        bookRepository.deleteAll();
        eventRepository.deleteAll();
        outboxRepository.deleteAll();
    }
}

