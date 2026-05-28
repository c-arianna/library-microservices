package mentoring.acomi.bookservice.domain.errors;

public class InvalidTitle extends ValidationDomain {

	private static final long serialVersionUID = 1635856226905060585L;

	private static final String type = "INVALID_TITLE";
	
	public InvalidTitle(String message) {
		super(type, message);
	}
	
	public static InvalidTitle empty() {
	    return new InvalidTitle("Title is required");
	 }

}
