package mentoring.acomi.sharedcodelibrary.event.handlers;

public class InvalidEventPayloadException extends RuntimeException {

	private static final long serialVersionUID = 4543367592864118179L;

	public InvalidEventPayloadException(String message) {
		super(message);
	}
}
