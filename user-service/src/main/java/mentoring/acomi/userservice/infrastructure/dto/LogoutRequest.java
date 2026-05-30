package mentoring.acomi.userservice.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(		
		@NotBlank(message = "refreshToken required")
		String refreshToken
) {}
