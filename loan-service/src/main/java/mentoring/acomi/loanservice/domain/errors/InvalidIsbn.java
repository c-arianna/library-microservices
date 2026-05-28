package mentoring.acomi.loanservice.domain.errors;

public class InvalidIsbn extends ValidationDomain {

	private static final long serialVersionUID = -2613570883180500587L;

	private static final String code = "INVALID_ISBN";

	public InvalidIsbn(String message) {
		super(code, message);
	}

	public static InvalidIsbn empty() {
		return new InvalidIsbn("ISBN is required");
	}

	public static InvalidIsbn invalidFormat(String isbn) {
		return new InvalidIsbn(String.format("Invalid ISBN format: %s", isbn));
	}

}
