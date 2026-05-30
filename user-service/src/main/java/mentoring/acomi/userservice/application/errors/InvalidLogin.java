package mentoring.acomi.userservice.application.errors;

import lombok.Getter;

@Getter
public class InvalidLogin  extends RuntimeException{

	private static final long serialVersionUID = 8951859935013883473L;

	private String code = "INVALID_LOGIN";
	
	public InvalidLogin(String message) {
		super(message);
	}
	
	public static InvalidLogin invalidCredentials(String email) {
	    return new InvalidLogin(String.format("Invalid Credentials: %s", email));
	 }

	public static InvalidLogin userSuspended(String email) {
		return new InvalidLogin(String.format("User is suspended: %s", email));
	}

	public static InvalidLogin userDisabled(String email) {
		return new InvalidLogin(String.format("User is disable: %s", email));
	}

}
