package mentoring.acomi.loanservice.application.reactor;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;

@Component
public class LoanReactor {
	
	private static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
	private static final String BOOK_NOT_REGISTERED = "BOOK_NOT_REGISTERED";
	private static final String RESERVATION_MISSING = "RESERVATION_MISSING";
	
	private final LoanEventRepository eventRepository;
	private final EventDispatcher eventDispatcher;
	
	private final Logger logger = LogManager.getLogger(LoanReactor.class);
	
	public LoanReactor(LoanEventRepository eventRepository, EventDispatcher eventDispatcher) {
		this.eventRepository = eventRepository;
		this.eventDispatcher = eventDispatcher;
	}
	
	public void handleBookReserved(BookLoanIntegrationPayload payload) {
		LoanAggregate loan = loadLoan(payload.loanId());
		loan.reserve();
	}

	public void handleBookReservationRejected(BookReservationRejectedIntegrationPayload payload) {
		LoanAggregate loan = loadLoan(payload.loanId());
		String reason = payload.reason();

		switch (reason) {
		case BOOK_NOT_AVAILABLE -> loan.fail(LoanFailedReason.BOOK_NOT_AVAILABLE);
		case BOOK_NOT_REGISTERED -> loan.fail(LoanFailedReason.BOOK_NOT_FOUND);
		default -> throw new IllegalArgumentException(String.format("Unknown reason: %s", reason));
		}

	}

	public void handleBookBorrowed(BookLoanIntegrationPayload payload) {
		LoanAggregate loan = loadLoan(payload.loanId());
		loan.confirm();
	}

	public void handleBookBorrowRejected(BookBorrowRejectedIntegrationPayload payload) {
		LoanAggregate loan = loadLoan(payload.loanId());
		String reason = payload.reason();

		switch (reason) {
		case BOOK_NOT_REGISTERED -> loan.fail(LoanFailedReason.BOOK_NOT_FOUND);
		case RESERVATION_MISSING -> loan.fail(LoanFailedReason.RESERVATION_MISSING);
		default -> throw new IllegalArgumentException(String.format("Unknown reason: %s", reason));
		}

	}
	
	private LoanAggregate loadLoan(String loanId) {

		List<LoanEvent> events = eventRepository.loadStream(loanId);
		Consumer<LoanEvent> dispatch = event -> {
			eventRepository.appendToStream(event);
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new LoanAggregate(loanId, dispatch, events);
	}


}
