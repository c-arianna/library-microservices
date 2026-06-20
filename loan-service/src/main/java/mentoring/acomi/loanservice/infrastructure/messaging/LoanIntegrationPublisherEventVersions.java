package mentoring.acomi.loanservice.infrastructure.messaging;

public final class LoanIntegrationPublisherEventVersions {

	public static final int LOAN_REQUESTED = 1;
	public static final int LOAN_CONFIRMED = 1;
	public static final int LOAN_CONFIRM_REQUESTED = 1;
	public static final int LOAN_CANCELED = 1;
	public static final int LOAN_RETURNED = 1;
	public static final int LOAN_RESERVED = 1;
	public static final int LOAN_FAILED = 1;
	
	private LoanIntegrationPublisherEventVersions() {}

}
