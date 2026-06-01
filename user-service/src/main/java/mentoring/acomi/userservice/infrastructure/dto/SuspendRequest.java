package mentoring.acomi.userservice.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;

public record SuspendRequest(
		
		@NotBlank(message = "userId required")
		String userId, 
		
		String reason
) {}
