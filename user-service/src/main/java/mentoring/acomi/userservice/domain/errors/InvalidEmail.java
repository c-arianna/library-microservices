package mentoring.acomi.userservice.domain.errors;

public class InvalidEmail extends ValidationDomain {

	private static final long serialVersionUID = 1635856226905060585L;

	private static final String type = "INVALID_EMAIL";
	
	public InvalidEmail(String message) {
		super(type, message);
	}
	
	public static InvalidEmail empty() {
	    return new InvalidEmail("Email is required");
	}

	public static InvalidEmail invalidFormat() {
	    return new InvalidEmail("Invalid email format");
	}
}
