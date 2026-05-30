package mentoring.acomi.userservice.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;

public record UnsubscribeRequest(
		@NotBlank(message = "userId required")
		String userId, 
		String reason
) {}
