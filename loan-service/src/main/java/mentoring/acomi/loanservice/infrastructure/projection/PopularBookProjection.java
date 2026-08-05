package mentoring.acomi.loanservice.infrastructure.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.application.view.PopularBookView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class PopularBookProjection implements EventProjector {

	private final BookViewRepository bookRepository;
	private final PopularBookViewRepository repository;

	public PopularBookProjection(BookViewRepository bookRepository, PopularBookViewRepository repository) {
		this.bookRepository = bookRepository;
		this.repository = repository;
	}

	@Override
	public boolean supports(IntegrationEventTypes eventType) {
		return eventType == IntegrationEventTypes.LOAN_CONFIRMED;
	}

	@Override
	public void project(IntegrationEventEnvelope<?> event) {
		
		if(event.eventType() == IntegrationEventTypes.LOAN_CONFIRMED) {
			LoanIntegrationPayload payload = (LoanIntegrationPayload) event.payload();
			BookView book = bookRepository.findByIsbn(payload.isbn()).orElseThrow();
			PopularBookView popularBook = new PopularBookView(book.isbn(), book.author(), book.title(), 1);
			repository.registerLoanCount(popularBook);
		}
		
	}

}
