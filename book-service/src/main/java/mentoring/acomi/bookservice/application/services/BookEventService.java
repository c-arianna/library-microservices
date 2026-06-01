package mentoring.acomi.bookservice.application.services;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.aggregates.BookAggregate;
import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.model.ISBN;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.loan.LoanIntegrationPayload;

@Service
public class BookEventService {

	private final Map<IntegrationEventTypes, Consumer<LoanIntegrationPayload>> handlers;

	private final BookEventRepository bookEventRepository;
	private final EventDispatcher eventDispatcher;
	private final Logger logger = LogManager.getLogger(BookEventService.class);

	public BookEventService(BookEventRepository eventRepository, EventDispatcher eventDispatcher) {
		this.bookEventRepository = eventRepository;
		this.eventDispatcher = eventDispatcher;
		this.handlers = Map.of(IntegrationEventTypes.LOAN_REQUESTED, this::handleLoanRequested,
				IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, this::handleLoanConfirmRequested, IntegrationEventTypes.LOAN_CANCELED,
				this::handleLoanCanceled, IntegrationEventTypes.LOAN_RETURNED, this::handleLoanReturned);
	}

	public void handle(IntegrationEventTypes type, LoanIntegrationPayload payload) {
		Consumer<LoanIntegrationPayload> handler = handlers.get(type);
		if (handler == null) {
			throw new IllegalArgumentException("Unsupported event type: " + type);
		}
		handler.accept(payload);
	}

	private void handleLoanRequested(LoanIntegrationPayload payload) {
		BookAggregate book = loadBook(payload.isbn());
		book.reserve(payload.loanId(), payload.userId());
	}

	private void handleLoanConfirmRequested(LoanIntegrationPayload payload) {
		BookAggregate book = loadBook(payload.isbn());
		book.borrow(payload.loanId(), payload.userId());
	}

	private void handleLoanCanceled(LoanIntegrationPayload payload) {

		String loanId = payload.loanId();

		try {
			BookAggregate book = loadBook(payload.isbn());
			book.release(loanId, payload.userId());
		} catch (Exception e) {
			logger.warn("Ignoring error on LoanCanceled, loanId={}", loanId, e);
		}
	}

	private void handleLoanReturned(LoanIntegrationPayload payload) {

		String loanId = payload.loanId();

		try {
			BookAggregate book = loadBook(payload.isbn());
			book.returnBorrowed(loanId, payload.userId());

		} catch (Exception e) {
			logger.warn("Ignoring error on LoanReturned, loanId={}", loanId, e);
		}
	}

	private BookAggregate loadBook(String isbn) {

		List<BookEvent> events = bookEventRepository.loadStream(isbn);
		Consumer<BookEvent> dispatch = event -> {
			bookEventRepository.appendToStream(event);
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new BookAggregate(ISBN.of(isbn), dispatch, events);
	}

}
