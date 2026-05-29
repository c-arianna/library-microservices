package mentoring.acomi.userservice.domain.errors;

public class ValidationDomain extends DomainError{
	
	private static final long serialVersionUID = -8967459678463791707L;
	
	public ValidationDomain(String code, String message) {
		super(code, message);
	}

}
