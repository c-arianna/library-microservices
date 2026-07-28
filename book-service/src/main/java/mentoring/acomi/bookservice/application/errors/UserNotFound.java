package mentoring.acomi.bookservice.application.errors;

import lombok.Getter;

@Getter
public class UserNotFound extends RuntimeException {

	private static final long serialVersionUID = 6817599286997595676L;

	private final String code = "USER_NOT_FOUND";
	
	public UserNotFound(String message) {
		super(message);
	}

}
