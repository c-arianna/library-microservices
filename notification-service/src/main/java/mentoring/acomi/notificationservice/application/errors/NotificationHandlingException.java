package mentoring.acomi.notificationservice.application.errors;

public class NotificationHandlingException extends RuntimeException {
	
	static final long serialVersionUID = 6914895554612659793L;

	public NotificationHandlingException(String message) {
		super(message);
	}
}