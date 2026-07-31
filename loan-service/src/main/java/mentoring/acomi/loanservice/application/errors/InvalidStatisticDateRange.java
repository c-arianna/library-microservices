package mentoring.acomi.loanservice.application.errors;

import lombok.Getter;

@Getter
public class InvalidStatisticDateRange extends RuntimeException {

	private static final long serialVersionUID = 6817599286997595676L;

	private final String code = "INVALID_DATE_RANGE";
	
	public InvalidStatisticDateRange(String message) {
		super(message);
	}
}
