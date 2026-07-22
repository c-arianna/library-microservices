package mentoring.acomi.userservice.infrastructure.messaging;

public final class UserIntegrationPublisherEventVersions {

	public static final int USER_SUBSCRIBED = 2;
	public static final int USER_UNSUBSCRIBED = 1;
	public static final int USER_SUSPENDED = 1;
	public static final int USER_UNSUSPENDED = 1;
	public static final int LIBRARY_CARD_ASSIGNED = 1;
	
	private UserIntegrationPublisherEventVersions() {}
}
