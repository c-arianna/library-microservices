package mentoring.acomi.userservice.domain.errors;

public class InvalidPassword extends ValidationDomain {

	private static final long serialVersionUID = 1635856226905060585L;

	private static final String type = "INVALID_PASSWORD";
	
	public InvalidPassword(String message) {
		super(type, message);
	}
	
	public static InvalidPassword empty() {
	    return new InvalidPassword("Password is required");
	}

}