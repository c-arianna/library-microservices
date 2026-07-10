package mentoring.acomi.loanservice.infrastructure.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddLoanRequest(
		
		@NotBlank
		String isbn, 
		
		String userId, 
		
		@NotNull
		LocalDate startDate, 
		
		LocalDate endDate) {
	
}
