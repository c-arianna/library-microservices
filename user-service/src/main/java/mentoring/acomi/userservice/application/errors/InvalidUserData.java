package mentoring.acomi.userservice.application.errors;

import lombok.Getter;

@Getter
public class InvalidUserData extends RuntimeException {

	private static final long serialVersionUID = 2548563722805583062L;

	private String code = "INVALID_USER_DATA";

	public InvalidUserData(String message) {
		super(message);
	}

}
