package mentoring.acomi.sharedlibrary.integration.messaging;

public final class MessagingTopology {

	private MessagingTopology() {}

	public static final String EVENTS_EXCHANGE = "events.exchange";

	public static final String BOOK_QUEUE = "book-service.queue";
	public static final String LOAN_QUEUE = "loan-service.queue";
	public static final String USER_QUEUE = "user-service.queue";

}
