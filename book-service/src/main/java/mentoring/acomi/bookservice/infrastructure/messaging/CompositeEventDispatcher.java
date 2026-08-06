package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

    private final BookIntegrationEventPublisher publisher;
    private final BookRequestIntegrationEventPublisher requestPublisher;
    
    public CompositeEventDispatcher(BookIntegrationEventPublisher publisher, BookRequestIntegrationEventPublisher requestPublisher) {
        this.publisher = publisher;
        this.requestPublisher = requestPublisher;
    }

    @Override
    public void dispatch(BookEvent event) {
        publisher.dispatch(event);
    }
    
    @Override
    public void dispatch(BookRequestEvent event) {
    	requestPublisher.dispatch(event);
    }
}