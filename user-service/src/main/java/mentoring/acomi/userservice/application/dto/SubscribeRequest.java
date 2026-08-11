package mentoring.acomi.userservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubscribeRequest(
		
		 @NotBlank(message = "Name is required")
		 @Size(min = 3, max = 100)
		 String name,

		 @NotBlank(message = "Lastname is required")
		 @Size(min = 3, max = 100)
		 String lastname,

		 @NotBlank(message = "Email is required")
		 @Email(message = "Invalid email format")
		 String email,

		 @NotBlank(message = "Password is required")
		 @Size(min=8, max=100)
		 String password
		
) {}
