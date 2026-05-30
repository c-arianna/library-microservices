package mentoring.acomi.userservice.application.errors;

import lombok.Getter;

@Getter
public class InvalidRefreshToken extends RuntimeException{

	private static final long serialVersionUID = -2933538806153441732L;
	
	private String code = "INVALID_REFRESH_TOKEN";
	
	public InvalidRefreshToken(String message) {
		super(message);
	}

}
