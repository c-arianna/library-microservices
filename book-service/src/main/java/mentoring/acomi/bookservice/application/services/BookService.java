package mentoring.acomi.bookservice.application.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.aggregates.BookAggregate;
import mentoring.acomi.bookservice.application.aggregates.BookAggregateFactory;
import mentoring.acomi.bookservice.application.errors.BookNotFound;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.domain.book.model.Book;
import mentoring.acomi.bookservice.domain.errors.ApplicationConflict;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.BookDto;
import mentoring.acomi.bookservice.infrastructure.dto.BookResponse;
import mentoring.acomi.bookservice.infrastructure.dto.BooksResponse;
import mentoring.acomi.bookservice.infrastructure.dto.RemoveBookCopiesRequest;

@Service
public class BookService {

	private final BookEventRepository bookEventRepository;
	private final BookViewQueryRepository bookViewRepository;
	private final BookAggregateFactory aggregateFactory;	
	
	public BookService(BookEventRepository eventRepository,	BookViewQueryRepository bookViewRepository, 
			BookAggregateFactory aggregateFactory) {
		this.bookEventRepository = eventRepository;
		this.bookViewRepository = bookViewRepository;
		this.aggregateFactory = aggregateFactory;
	}

	@Transactional
	public BookResponse addBook(AddBookRequest request) {

		String isbn = request.isbn();

		if (bookEventRepository.exists(isbn, AggregateType.BOOK.name())) {
			throw new ApplicationConflict("BOOK_ALREADY_EXISTS", String.format("ISBN: %s", isbn));
		}

		BookAggregate aggregate = aggregateFactory.create(request.isbn());
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
		BookAggregate aggregate = aggregateFactory.create(isbn);
		aggregate.addCopies(request.quantity());
	}

	@Transactional
	public void removeBookCopies(RemoveBookCopiesRequest request, String isbn) {
		BookAggregate aggregate = aggregateFactory.create(isbn);
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
	
	public BookDto getBook(String isbn) {
		
		Optional<BookView> book = bookViewRepository.findById(isbn);
		
		if(book.isEmpty()) {
			throw new BookNotFound(String.format("%s not registered", isbn));
		}
		
		BookView bookView = book.get();
		
		return new BookDto(bookView.isbn(), bookView.author(), bookView.title(), bookView.description(), bookView.totalCopies(), 
				bookView.borrowedCopies(), bookView.reservedCopies(), bookView.availableCopies() > 0);
	}

}
