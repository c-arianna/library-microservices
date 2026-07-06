package mentoring.acomi.bookservice.application.services;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.aggregates.BookAggregate;
import mentoring.acomi.bookservice.application.errors.BookNotFound;
import mentoring.acomi.bookservice.application.messaging.EventDispatcher;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.domain.errors.ApplicationConflict;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.domain.model.Book;
import mentoring.acomi.bookservice.domain.model.ISBN;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.BookDto;
import mentoring.acomi.bookservice.infrastructure.dto.BookResponse;
import mentoring.acomi.bookservice.infrastructure.dto.BooksResponse;
import mentoring.acomi.bookservice.infrastructure.dto.RemoveBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationConsumerEventVersions;

@Service
public class BookService {

	private final BookEventRepository bookEventRepository;
	private final EventDispatcher eventDispatcher;
	private final BookViewQueryRepository bookViewRepository;
	private final Logger logger = LogManager.getLogger(BookService.class);
	
	public BookService(BookEventRepository eventRepository, EventDispatcher eventDispatcher,
			BookViewQueryRepository bookViewRepository) {
		this.bookEventRepository = eventRepository;
		this.eventDispatcher = eventDispatcher;
		this.bookViewRepository = bookViewRepository;
	}

	@Transactional
	public BookResponse addBook(AddBookRequest request) {

		String isbn = request.isbn();

		if (bookEventRepository.exists(isbn, AggregateType.BOOK.name())) {
			throw new ApplicationConflict("BOOK_ALREADY_EXISTS", String.format("ISBN: %s", isbn));
		}

		BookAggregate aggregate = loadBook(request.isbn());
		Book book = getBook(request);
		aggregate.register(book);
		return new BookResponse(book.getIsbn().formatted());

	}

	public BooksResponse findBooks(BookFilter filter) {
		List<BookView> books = bookViewRepository.find(filter);
		return toBooksResponse(books);
	}

	@Transactional
	public void addBookCopies(AddBookCopiesRequest request, String isbn) {
		BookAggregate aggregate = loadBook(isbn);
		aggregate.addCopies(request.quantity());
	}

	@Transactional
	public void removeBookCopies(RemoveBookCopiesRequest request, String isbn) {
		BookAggregate aggregate = loadBook(isbn);
		aggregate.removeCopies(request.quantity(), request.reason());
	}
	
	private Book getBook(AddBookRequest request) {
		return Book.create(request.isbn(), request.author(), request.title(), request.description());
	}

	private BooksResponse toBooksResponse(List<BookView> books) {

		List<BookDto> bookResponse = books.stream().map(b -> new BookDto(b.isbn(), b.author(), b.title(),
				b.description(), b.totalCopies(), b.borrowedCopies(), b.reservedCopies(),   b.availableCopies() > 0)).toList();

		return new BooksResponse(bookResponse);
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
	
	public BookDto getBook(String isbn) {
		
		Optional<BookView> book = bookViewRepository.findById(isbn);
		
		if(book.isEmpty()) {
			throw new BookNotFound(String.format("%s not registered", isbn));
		}
		
		BookView bookView = book.get();
		
		return new BookDto(bookView.isbn(), bookView.author(), bookView.title(), bookView.description(), bookView.totalCopies(), 
				bookView.borrowedCopies(), bookView.reservedCopies(), bookView.availableCopies() > 0);
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
