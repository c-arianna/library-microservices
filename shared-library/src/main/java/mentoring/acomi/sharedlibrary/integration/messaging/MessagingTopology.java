package mentoring.acomi.sharedlibrary.integration.messaging;

public final class MessagingTopology {

	private MessagingTopology() {}

	public static final String EVENTS_EXCHANGE = "events.exchange";

	public static final String BOOK_QUEUE = "book-service.queue";
	public static final String LOAN_QUEUE = "loan-service.queue";
	public static final String USER_QUEUE = "user-service.queue";
	public static final String REPLAY_BOOK_QUEUE = "replay.book.queue";
	public static final String REPLAY_LOAN_QUEUE = "replay.loan.queue";
	public static final String REPLAY_USER_QUEUE = "replay.user.queue";
	public static final String NOTIFICATION_QUEUE = "notification.queue";

}
