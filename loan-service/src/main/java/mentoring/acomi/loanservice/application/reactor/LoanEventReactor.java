package mentoring.acomi.loanservice.application.reactor;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;
import mentoring.acomi.loanservice.infrastructure.messaging.LoanIntegrationPublisherEventVersions;

@Component
public class LoanEventReactor {
	
	private static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
	private static final String BOOK_NOT_REGISTERED = "BOOK_NOT_REGISTERED";
	private static final String RESERVATION_MISSING = "RESERVATION_MISSING";
	
	private final LoanEventRepository eventRepository;
	private final EventDispatcher eventDispatcher;
	
	private final Logger logger = LogManager.getLogger(LoanEventReactor.class);
	
	public LoanEventReactor(LoanEventRepository eventRepository, EventDispatcher eventDispatcher) {
		this.eventRepository = eventRepository;
		this.eventDispatcher = eventDispatcher;
	}
	
	public void handleBookReserved(CommandBookEvent command) {
		LoanAggregate loan = loadLoan(command.loanId());
		loan.reserve();
	}

	public void handleBookReservationRejected(CommandBookRejectedEvent command) {
		LoanAggregate loan = loadLoan(command.loanId());
		String reason = command.reason();

		switch (reason) {
		case BOOK_NOT_AVAILABLE -> loan.fail(LoanFailedReason.BOOK_NOT_AVAILABLE);
		case BOOK_NOT_REGISTERED -> loan.fail(LoanFailedReason.BOOK_NOT_FOUND);
		default -> throw new IllegalArgumentException(String.format("Unknown reason: %s", reason));
		}

	}

	public void handleBookBorrowed(CommandBookEvent command) {
		LoanAggregate loan = loadLoan(command.loanId());
		loan.confirm();
	}

	public void handleBookBorrowRejected(CommandBookRejectedEvent command) {
		LoanAggregate loan = loadLoan(command.loanId());
		String reason = command.reason();

		switch (reason) {
		case BOOK_NOT_REGISTERED -> loan.fail(LoanFailedReason.BOOK_NOT_FOUND);
		case RESERVATION_MISSING -> loan.fail(LoanFailedReason.RESERVATION_MISSING);
		default -> throw new IllegalArgumentException(String.format("Unknown reason: %s", reason));
		}

	}
	
	private LoanAggregate loadLoan(String loanId) {

		List<LoanEvent> events = eventRepository.loadStream(loanId);
		Consumer<LoanEvent> dispatch = event -> {
			eventRepository.appendToStream(event, getSchemaVersion(event.type()));
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new LoanAggregate(loanId, dispatch, events);
	}

	private int getSchemaVersion(LoanEventType type) {

		return switch(type) {
		
		case LoanRequested -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_REQUESTED;
		}
		case LoanFailed -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_FAILED;
		}
		case LoanReserved -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_RESERVED;
		}
		case LoanConfirmed -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_CONFIRMED;
		}
		case LoanCanceled -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_CANCELED;
		}
		case LoanReturned -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_RETURNED;
		}
		case LoanConfirmRequested -> {
			yield LoanIntegrationPublisherEventVersions.LOAN_CONFIRM_REQUESTED;
		}
		};
	}

}
