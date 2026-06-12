package mentoring.acomi.userservice.application.errors;

import lombok.Getter;

@Getter
public class InvalidUser extends RuntimeException {

	private static final long serialVersionUID = 6817599286997595676L;

	private final String code = "INVALID_USER";
	
	public InvalidUser(String message) {
		super(message);
	}

}