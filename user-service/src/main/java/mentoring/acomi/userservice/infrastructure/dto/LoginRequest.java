package mentoring.acomi.userservice.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

		@NotBlank(message = "email required")
		String email,

		@NotBlank(message = "password required")
		String password) {
}
