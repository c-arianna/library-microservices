package mentoring.acomi.userservice.infrastructure.dto;

import jakarta.validation.constraints.NotEmpty;

public record LogoutRequest(		
		@NotEmpty
		String refreshToken
) {}
