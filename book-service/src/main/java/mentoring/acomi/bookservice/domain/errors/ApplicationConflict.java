package mentoring.acomi.bookservice.domain.errors;

public class ApplicationConflict extends DomainError {

	private static final long serialVersionUID = 824313013090108769L;
		
	public ApplicationConflict(String code, String message) {
		super(code, message);
	}
	
}
