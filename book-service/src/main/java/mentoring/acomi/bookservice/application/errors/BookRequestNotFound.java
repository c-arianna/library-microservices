package mentoring.acomi.bookservice.application.errors;

import lombok.Getter;

@Getter
public class BookRequestNotFound extends RuntimeException {

	private static final long serialVersionUID = -4993396886002224816L;
	
	private final String code = "BOOK_REQUEST_NOT_FOUND";
	
	public BookRequestNotFound(String message) {
		super(message);
	}


}
