package mentoring.acomi.bookservice.application.reactor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.aggregates.BookAggregate;
import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.model.ISBN;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookEventReactor {

	private final Map<IntegrationEventTypes, Consumer<CommandLoanEvent>> handlers;

	private final BookEventRepository bookEventRepository;
	private final EventDispatcher eventDispatcher;
	private final Logger logger = LogManager.getLogger(BookEventReactor.class);

	public BookEventReactor(BookEventRepository eventRepository, EventDispatcher eventDispatcher) {
		this.bookEventRepository = eventRepository;
		this.eventDispatcher = eventDispatcher;
		this.handlers = Map.of(IntegrationEventTypes.LOAN_REQUESTED, this::handleLoanRequested,
				IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, this::handleLoanConfirmRequested,
				IntegrationEventTypes.LOAN_CANCELED, this::handleLoanCanceled, IntegrationEventTypes.LOAN_RETURNED,
				this::handleLoanReturned);
	}

	public void handle(IntegrationEventTypes eventType, CommandLoanEvent command) {
		Consumer<CommandLoanEvent> handler = handlers.get(eventType);
		if (handler == null) {
			throw new IllegalArgumentException(String.format("Unsupported event type: %s", eventType));
		}
		handler.accept(command);

	}

	private void handleLoanRequested(CommandLoanEvent command) {
		BookAggregate book = loadBook(command.isbn());
		book.reserve(command.loanId(), command.userId());
	}

	private void handleLoanConfirmRequested(CommandLoanEvent command) {
		BookAggregate book = loadBook(command.isbn());
		book.borrow(command.loanId(), command.userId());
	}

	private void handleLoanCanceled(CommandLoanEvent command) {

		String loanId = command.loanId();

		try {
			BookAggregate book = loadBook(command.isbn());
			book.release(loanId, command.userId());
		} catch (Exception e) {
			logger.warn("Ignoring error on LoanCanceled, loanId={}", loanId, e);
		}
	}

	private void handleLoanReturned(CommandLoanEvent command) {

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
