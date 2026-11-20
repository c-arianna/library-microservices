package mentoring.acomi.assistantservice.application.errors;

public class NonRetryableEventException extends RuntimeException {
	
	private static final long serialVersionUID = -1034152653386602722L;

	public NonRetryableEventException(String message) {
        super(message);
    }
}