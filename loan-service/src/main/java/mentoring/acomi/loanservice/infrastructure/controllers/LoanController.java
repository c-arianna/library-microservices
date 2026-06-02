package mentoring.acomi.loanservice.infrastructure.controllers;

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
import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.services.LoanService;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;
import mentoring.acomi.loanservice.infrastructure.dto.LoansResponse;

@RestController
public class LoanController {

	private final LoanService service;

	public LoanController(LoanService service) {
		this.service = service;
	}

	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'READER')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LoanResponse addLoan(@RequestBody @Valid AddLoanRequest request) {
		return service.addLoan(request);
	}

	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{loanId}/confirm")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void confirmLoan(@PathVariable String loanId) {
		service.confirmLoan(loanId);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{loanId}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelLoan(@PathVariable String loanId) {
		service.cancelLoan(loanId);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/{loanId}/return")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void returnLoan(@PathVariable String loanId) {
		service.returnLoan(loanId);
	}
	
	@PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
	@GetMapping
	public LoansResponse findLoans(@RequestParam(required = false) String userId, @RequestParam(required = false) String isbn,
		    @RequestParam(required = false) LoanStatus status) {
	    LoanFilter loanFilter = new LoanFilter(isbn, userId, status);
		return service.findLoans(loanFilter);
	}

}
