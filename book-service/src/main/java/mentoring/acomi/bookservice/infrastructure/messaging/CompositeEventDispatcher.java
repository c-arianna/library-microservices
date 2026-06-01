package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookStateEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

    private final BookProjection projection;
    private final BookIntegrationEventPublisher publisher;

    public CompositeEventDispatcher(BookProjection projection, BookIntegrationEventPublisher publisher) {
        this.projection = projection;
        this.publisher = publisher;
    }

    @Override
    public void dispatch(BookEvent event) {

        if (event instanceof BookStateEvent stateEvent) {
            projection.updateView(stateEvent);
        }

        publisher.dispatch(event);
    }
}