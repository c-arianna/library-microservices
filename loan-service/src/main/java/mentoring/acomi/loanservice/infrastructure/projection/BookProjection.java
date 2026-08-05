package mentoring.acomi.loanservice.infrastructure.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookProjection implements EventProjector {

	private final BookViewRepository repository;
	
	public BookProjection(BookViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean supports(IntegrationEventTypes eventType) {
		return eventType == IntegrationEventTypes.BOOK_REGISTERED;
	}

	@Override
	public void project(IntegrationEventEnvelope<?> event) {
		if(event.eventType() == IntegrationEventTypes.BOOK_REGISTERED) {
			BookRegisteredIntegrationPayload payload = (BookRegisteredIntegrationPayload) event.payload();
			BookView book = new BookView(payload.isbn(), payload.author(), payload.title());
			repository.insert(book);
		}
	}

}