package mentoring.acomi.bookservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddBookRequestDto(
				
		@NotBlank(message = "author required") 
		@Size(min = 3, max = 100) String author,

		@NotBlank(message = "title required") 
		@Size(min = 3, max = 100) String title,

		String isbn,
		
		String notes) {

}
