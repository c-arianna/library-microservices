package mentoring.acomi.bookservice.domain.errors;

public class InvalidAuthor extends ValidationDomain {

	private static final long serialVersionUID = 1635856226905060585L;

	private static final String type = "INVALID_AUTHOR";
	
	public InvalidAuthor(String message) {
		super(type, message);
	}
	
	public static InvalidAuthor empty() {
	    return new InvalidAuthor("Author is required");
	 }

}
