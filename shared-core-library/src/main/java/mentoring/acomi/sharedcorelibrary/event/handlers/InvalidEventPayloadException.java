package mentoring.acomi.sharedcorelibrary.event.handlers;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;

public class InvalidEventPayloadException extends RuntimeException {

	private static final long serialVersionUID = 4543367592864118179L;

	private final Set<String> invalidFields;

	public InvalidEventPayloadException(Set<? extends ConstraintViolation<?>> violations) {
		super("Invalid payload");
		this.invalidFields = violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
	}
	
	public Set<String> getInvalidFields() {
		return invalidFields;
	}
}
