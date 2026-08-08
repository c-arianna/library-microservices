package mentoring.acomi.bookservice.domain.errors;

public class InvalidBookRequestStateTransition extends DomainError {

	private static final long serialVersionUID = 3499833485930253741L;
	
	private static final String code = "INVALID_REQUEST_STATE_TRANSITION";
	
	public InvalidBookRequestStateTransition(String message) {
		super(code, message);
	}

}
