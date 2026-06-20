package mentoring.acomi.bookservice.infrastructure.messaging;

public final class BookIntegrationPublisherEventVersions {

	public static final int BOOK_REGISTERED = 1;
	public static final int BOOK_COPIES_UPDATED = 1;
	public static final int BOOK_RESERVED = 1;
	public static final int BOOK_BORROWED = 1;
	public static final int BOOK_RELEASED = 1;
	public static final int BOOK_RETURNED = 1;
	public static final int BOOK_RESERVATION_REJECTED = 1;
	public static final int BOOK_BORROW_REJECTED = 1;

	private BookIntegrationPublisherEventVersions() {}

}
