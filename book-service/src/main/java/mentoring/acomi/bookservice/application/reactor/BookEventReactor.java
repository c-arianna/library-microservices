package mentoring.acomi.bookservice.application.reactor;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.aggregates.BookAggregate;
import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.domain.model.ISBN;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationConsumerEventVersions;

@Component
public class BookEventReactor {

	private final BookEventRepository bookEventRepository;
	private final EventDispatcher eventDispatcher;
	private final Logger logger = LogManager.getLogger(BookEventReactor.class);

	public BookEventReactor(BookEventRepository eventRepository, EventDispatcher eventDispatcher) {
		this.bookEventRepository = eventRepository;
		this.eventDispatcher = eventDispatcher;
	}

	public void handleLoanRequested(CommandLoanEvent command) {
		BookAggregate book = loadBook(command.isbn());
		book.reserve(command.loanId(), command.userId());
	}

	public void handleLoanConfirmRequested(CommandLoanEvent command) {
		BookAggregate book = loadBook(command.isbn());
		book.borrow(command.loanId(), command.userId());
	}

	public void handleLoanCanceled(CommandLoanEvent command) {

		String loanId = command.loanId();

		try {
			BookAggregate book = loadBook(command.isbn());
			book.release(loanId, command.userId());
		} catch (Exception e) {
			logger.warn("Ignoring error on LoanCanceled, loanId={}", loanId, e);
		}
	}

	public void handleLoanReturned(CommandLoanEvent command) {

		String loanId = command.loanId();

		try {
			BookAggregate book = loadBook(command.isbn());
			book.returnBorrowed(loanId, command.userId());

		} catch (Exception e) {
			logger.warn("Ignoring error on LoanReturned, loanId={}", loanId, e);
		}
	}

	private BookAggregate loadBook(String isbn) {

		List<BookEvent> events = bookEventRepository.loadStream(isbn);
		Consumer<BookEvent> dispatch = event -> {
			bookEventRepository.appendToStream(event, getSchemaVersion(event.type()));
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new BookAggregate(ISBN.of(isbn), dispatch, events);
	}
	
	private int getSchemaVersion(BookEventType eventType) {
		return switch(eventType) {
		
		case BookBorrowRejected -> {
			yield BookIntegrationConsumerEventVersions.BOOK_BORROW_REJECTED;
		}
		case BookBorrowed-> {
			yield BookIntegrationConsumerEventVersions.BOOK_BORROWED;
		}
		case BookCopiesAdded-> {
			yield BookIntegrationConsumerEventVersions.BOOK_COPIES_UPDATED;
		}
		case BookCopiesRemoved-> {
			yield BookIntegrationConsumerEventVersions.BOOK_COPIES_UPDATED;
		}
		case BookRegistered-> {
			yield BookIntegrationConsumerEventVersions.BOOK_REGISTERED;
		}
		case BookReleased-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RELEASED;
		}
		case BookReservationRejected-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RESERVATION_REJECTED;
		}
		case BookReserved-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RESERVED;
		}
		case BookReturned-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RETURNED;
		}
		
		};
	}

}
