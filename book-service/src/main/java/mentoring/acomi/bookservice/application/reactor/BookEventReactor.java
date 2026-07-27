package mentoring.acomi.bookservice.application.reactor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.aggregates.BookAggregate;
import mentoring.acomi.bookservice.application.aggregates.BookAggregateFactory;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;

@Component
public class BookEventReactor {

	private final BookAggregateFactory aggregateFactory;
	
	private final Logger logger = LogManager.getLogger(BookEventReactor.class);

	public BookEventReactor(BookAggregateFactory aggregateFactory) {
		this.aggregateFactory = aggregateFactory;
		
	}

	public void handleLoanRequested(CommandLoanEvent command) {
		BookAggregate book = aggregateFactory.create(command.isbn());
		book.reserve(command.loanId(), command.userId());
	}

	public void handleLoanConfirmRequested(CommandLoanEvent command) {
		BookAggregate book = aggregateFactory.create(command.isbn());
		book.borrow(command.loanId(), command.userId());
	}

	public void handleLoanCanceled(CommandLoanEvent command) {

		String loanId = command.loanId();

		try {
			BookAggregate book = aggregateFactory.create(command.isbn());
			book.release(loanId, command.userId());
		} catch (Exception e) {
			logger.warn("Ignoring error on LoanCanceled, loanId={}", loanId, e);
		}
	}

	public void handleLoanReturned(CommandLoanEvent command) {

		String loanId = command.loanId();

		try {
			BookAggregate book = aggregateFactory.create(command.isbn());
			book.returnBorrowed(loanId, command.userId());

		} catch (Exception e) {
			logger.warn("Ignoring error on LoanReturned, loanId={}", loanId, e);
		}
	}
	
}
