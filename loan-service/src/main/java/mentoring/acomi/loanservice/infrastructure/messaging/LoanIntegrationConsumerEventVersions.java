package mentoring.acomi.loanservice.infrastructure.messaging;

public final class LoanIntegrationConsumerEventVersions {

	public static final int LOAN_REQUESTED = 1;
	public static final int LOAN_CONFIRMED = 1;
	public static final int LOAN_CANCELED = 1;
	public static final int LOAN_RETURNED = 1;
	public static final int LOAN_RESERVED = 1;
	public static final int LOAN_FAILED = 1;
	
	public static final int BOOK_RESERVED = 1;
	public static final int BOOK_RESERVATION_REJECTED = 1;
	public static final int BOOK_BORROWED = 1;
	public static final int BOOK_BORROW_REJECTED = 1;
	
	public static final int USER_SUBSCRIBED = 1;
	public static final int USER_UNSUBSCRIBED = 1;
	public static final int USER_SUSPENDED = 1;
	public static final int USER_UNSUSPENDED = 1;
	
	private LoanIntegrationConsumerEventVersions() {}

}
