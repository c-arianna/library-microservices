package mentoring.acomi.loanservice.domain.errors;

public class InvalidLoanStateTransition extends DomainError {

	private static final long serialVersionUID = 9103344839514959276L;
	
	private static final String code = "INVALID_STATE_TRANSATION";
	
	public InvalidLoanStateTransition(String message) {
		super(code, message);
	}

}
