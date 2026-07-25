package mentoring.acomi.loanservice.application.reactor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.aggregates.LoanAggregateFactory;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;

@Component
public class LoanEventReactor {
	
	private static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";
	private static final String BOOK_NOT_REGISTERED = "BOOK_NOT_REGISTERED";
	private static final String RESERVATION_MISSING = "RESERVATION_MISSING";
	
	private final LoanAggregateFactory aggregateFactory;
	
	public LoanEventReactor(LoanAggregateFactory aggregateFactory) {
		this.aggregateFactory = aggregateFactory;
	}
	
	@Transactional
	public void handleBookReserved(CommandBookEvent command) {
		LoanAggregate loan = aggregateFactory.create(command.loanId());
		loan.reserve();
	}

	@Transactional
	public void handleBookReservationRejected(CommandBookRejectedEvent command) {
		LoanAggregate loan = aggregateFactory.create(command.loanId());
		String reason = command.reason();

		switch (reason) {
		case BOOK_NOT_AVAILABLE -> loan.fail(LoanFailedReason.BOOK_NOT_AVAILABLE);
		case BOOK_NOT_REGISTERED -> loan.fail(LoanFailedReason.BOOK_NOT_FOUND);
		default -> throw new IllegalArgumentException(String.format("Unknown reason: %s", reason));
		}

	}

	@Transactional
	public void handleBookBorrowed(CommandBookEvent command) {
		LoanAggregate loan = aggregateFactory.create(command.loanId());
		loan.confirm();
	}

	@Transactional
	public void handleBookBorrowRejected(CommandBookRejectedEvent command) {
		LoanAggregate loan = aggregateFactory.create(command.loanId());
		String reason = command.reason();

		switch (reason) {
		case BOOK_NOT_REGISTERED -> loan.fail(LoanFailedReason.BOOK_NOT_FOUND);
		case RESERVATION_MISSING -> loan.fail(LoanFailedReason.RESERVATION_MISSING);
		default -> throw new IllegalArgumentException(String.format("Unknown reason: %s", reason));
		}

	}

}
