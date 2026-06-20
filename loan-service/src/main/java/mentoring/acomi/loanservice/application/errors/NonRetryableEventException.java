package mentoring.acomi.loanservice.application.errors;

public class NonRetryableEventException extends RuntimeException {
	
	private static final long serialVersionUID = -733465465162631189L;

	public NonRetryableEventException(String message) {
        super(message);
    }
}
