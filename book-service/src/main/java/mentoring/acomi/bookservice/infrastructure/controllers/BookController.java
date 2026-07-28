package mentoring.acomi.bookservice.infrastructure.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.services.BookService;
import mentoring.acomi.bookservice.application.services.SubscriptionService;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddSubscriptionRequest;
import mentoring.acomi.bookservice.infrastructure.dto.BookDto;
import mentoring.acomi.bookservice.infrastructure.dto.BookResponse;
import mentoring.acomi.bookservice.infrastructure.dto.BookSubscriptionsResponse;
import mentoring.acomi.bookservice.infrastructure.dto.BooksResponse;
import mentoring.acomi.bookservice.infrastructure.dto.RemoveBookCopiesRequest;

@RestController
public class BookController {

	private final BookService service;
	private final SubscriptionService subscriptionService;
	
	public BookController(BookService service, SubscriptionService subscriptionService) {
		this.service = service;
		this.subscriptionService = subscriptionService;
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/")
	@ResponseStatus(HttpStatus.CREATED)
	public BookResponse addBook(@RequestBody @Valid AddBookRequest request) {
		return service.addBook(request);
	}
	
	@PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
	@GetMapping("/")
	public BooksResponse findBooks(@RequestParam(required = false) String title, 
			@RequestParam(required = false) String author, @RequestParam(required = false) String isbn,
		    @RequestParam(required = false) boolean onlyAvailable){
		BookFilter filter = new BookFilter(title, author, isbn, onlyAvailable);
		return service.findBooks(filter);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{isbn}/copies/add")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void addBookCopies(@RequestBody AddBookCopiesRequest request, @PathVariable String isbn) {
		service.addBookCopies(request, isbn);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{isbn}/copies/remove")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeBookCopies(@RequestBody RemoveBookCopiesRequest request, @PathVariable String isbn) {
		service.removeBookCopies(request, isbn);
	}
	
	@PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
	@GetMapping("/{isbn}")
	public BookDto getBook(@PathVariable String isbn){
		return service.getBook(isbn);
	}
	
	@PreAuthorize("hasAnyRole('READER')")
	@PostMapping("/{isbn}/subscription")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void addSubscription(@RequestBody AddSubscriptionRequest request, @PathVariable String isbn) {
		subscriptionService.addSubscription(request, isbn);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/{isbn}/subscriptions")
	public BookSubscriptionsResponse getSubscriptions(@PathVariable String isbn){
		return subscriptionService.getSubscriptions(isbn);
	}
	
}
