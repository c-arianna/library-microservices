package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.domain.events.BookEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

    private final BookIntegrationEventPublisher publisher;

    public CompositeEventDispatcher(BookIntegrationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void dispatch(BookEvent event) {
        publisher.dispatch(event);
    }
}