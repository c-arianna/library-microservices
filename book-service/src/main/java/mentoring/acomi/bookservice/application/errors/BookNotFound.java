package mentoring.acomi.bookservice.application.errors;

import lombok.Getter;

@Getter
public class BookNotFound extends RuntimeException {

	private static final long serialVersionUID = 6817599286997595676L;

	private final String code = "BOOK_NOT_FOUND";
	
	public BookNotFound(String message) {
		super(message);
	}

}