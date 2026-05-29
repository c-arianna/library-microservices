package mentoring.acomi.userservice.domain.model;

import lombok.Getter;
import mentoring.acomi.userservice.domain.errors.InvalidEmail;

@Getter
public class Email {

	private String value;

	public Email(String value) {
		this.value = this.validate(value);
	}

	public String validate(String value) {

		if (value == null || value.isBlank()) {
			throw InvalidEmail.empty();
		}

		if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			throw InvalidEmail.invalidFormat();
		}

		return value;

	}

}
