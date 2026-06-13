package mentoring.acomi.userservice.application.errors;

import lombok.Getter;

@Getter
public class UserCreationError extends RuntimeException {
		
	private static final long serialVersionUID = 2532748524837056473L;
	
	private String code = "IDENTITY_PROVIDER_ERROR";
	
	public UserCreationError(String message) {
		super(message);
	}
	
}