package mentoring.acomi.bookservice.infrastructure.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.bookservice.application.dto.BookRequestAddedResponse;
import mentoring.acomi.bookservice.application.dto.BookRequestDetailDto;
import mentoring.acomi.bookservice.application.dto.BookRequestDto;
import mentoring.acomi.bookservice.application.dto.BookRequestRejectDto;
import mentoring.acomi.bookservice.application.services.BookRequestService;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequestDto;

@RestController
@RequestMapping("/requests")
public class BookRequestController {

	private final BookRequestService service;

	public BookRequestController(BookRequestService service) {
		this.service = service;
	}

	@PreAuthorize("hasAnyRole('READER')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookRequestAddedResponse add(@RequestBody @Valid AddBookRequestDto request) {
		return service.addRequest(request);
	}
	
	@PreAuthorize("hasAnyRole('READER', 'ADMIN', 'LIBRARIAN')")
	@GetMapping
	public List<BookRequestDto> getBookRequests() {
		return service.getBookRequests();
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{bookRequestId}/approve")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approve(@PathVariable String bookRequestId) {
		service.approveBookRequest(bookRequestId);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{bookRequestId}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reject(@RequestBody BookRequestRejectDto request, @PathVariable String bookRequestId) {
		service.rejectBookRequest(request, bookRequestId);
	}
	
	@PreAuthorize("hasAnyRole('READER')")
	@PostMapping("/{bookRequestId}/vote")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void voteBookRequest(@PathVariable String bookRequestId) {
		service.voteBookRequest(bookRequestId);
	}
	
	@PreAuthorize("hasAnyRole('READER', 'ADMIN', 'LIBRARIAN')")
	@GetMapping("/{bookRequestId}")
	public BookRequestDetailDto getBookRequestDetail(@PathVariable String bookRequestId) {
		return service.getBookRequestDetail(bookRequestId);		
	}
	
}
