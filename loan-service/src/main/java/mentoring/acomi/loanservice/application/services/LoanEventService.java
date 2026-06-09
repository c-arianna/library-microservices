package mentoring.acomi.loanservice.application.services;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;

@Service
public class LoanEventService {

	private static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
	private static final String BOOK_NOT_REGISTERED = "BOOK_NOT_REGISTERED";
	private static final String RESERVATION_MISSING = "RESERVATION_MISSING";

	private final LoanEventRepository eventRepository;
	private final UserViewRepository userViewRepository;
	private final EventDispatcher eventDispatcher;
	private final Logger logger = LogManager.getLogger(LoanEventService.class);

	public LoanEventService(LoanEventRepository eventRepository, UserViewRepository userViewRepository, EventDispatcher eventDispatcher) {
		this.eventRepository = eventRepository;
		this.userViewRepository = userViewRepository;
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
	
	@Transactional
	public void handleSubscribeUser(UserSubscribedIntegrationPayload payload) {
		UserView user = new UserView(payload.userId(), payload.email(), payload.status());
		userViewRepository.add(user);
	}

	@Transactional
	public void handleUpdateUserStatus(UserIntegrationPayload payload) {
		userViewRepository.updateStatus(payload.userId(), payload.status());
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
