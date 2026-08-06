package mentoring.acomi.bookservice.infrastructure.messaging;

public final class BookIntegrationConsumerEventVersions {

	public static final int BOOK_REGISTERED = 1;
	public static final int BOOK_COPIES_UPDATED = 1;
	public static final int BOOK_RESERVED = 1;
	public static final int BOOK_BORROWED = 1;
	public static final int BOOK_RELEASED = 1;
	public static final int BOOK_RETURNED = 1;
	public static final int BOOK_RESERVATION_REJECTED = 1;
	public static final int BOOK_BORROW_REJECTED = 1;
	
	public static final int BOOK_REQUEST_ADDED = 1;
	public static final int BOOK_REQUEST_VOTED = 1;
	public static final int BOOK_REQUEST_APPROVED = 1;
	public static final int BOOK_REQUEST_REJECTED = 1;
	
	public static final int LOAN_REQUESTED = 1;
	public static final int LOAN_CONFIRM_REQUESTED = 1;
	public static final int LOAN_CANCELED = 1;
	public static final int LOAN_RETURNED = 1;

	private BookIntegrationConsumerEventVersions() {}

}
