package mentoring.acomi.loanservice.application.errors;

import lombok.Getter;

@Getter
public class LoanNotFound extends RuntimeException {

	private static final long serialVersionUID = 6817599286997595676L;

	private final String code = "LOAN_NOT_FOUND";
	
	public LoanNotFound(String message) {
		super(message);
	}

}