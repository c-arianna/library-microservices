package mentoring.acomi.assistantservice.application.errors;

import lombok.Getter;

@Getter
public class BookNotFound extends RuntimeException {

	private static final long serialVersionUID = 8628088285376719099L;
	
	private final String code = "BOOK_NOT_FOUND";
	
	public BookNotFound(String message) {
		super(message);
	}

}