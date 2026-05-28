package mentoring.acomi.loanservice.domain.errors;

public class InvalidLoanId extends ValidationDomain{

	private static final long serialVersionUID = -2285634562165497451L;

	private static final String code = "INVALID_ID";
	
	public InvalidLoanId(String message) {
		super(code, message);
	}

}
