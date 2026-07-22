package mentoring.acomi.userservice.infrastructure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mentoring.acomi.sharedcorelibrary.model.UserRole;

public record UserRegisterRequest (

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
	 String password,
	 
	 @NotNull
	 UserRole role
	 
	 
) {}
