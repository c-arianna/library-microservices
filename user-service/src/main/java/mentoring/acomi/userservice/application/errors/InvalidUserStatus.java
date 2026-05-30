package mentoring.acomi.userservice.application.errors;

import lombok.Getter;

@Getter
public class InvalidUserStatus extends RuntimeException {

	private static final long serialVersionUID = 8800606425694791863L;

	private String code = "INVALID_USER_STATUS";

	public InvalidUserStatus(String message) {
		super(message);
	}

}
