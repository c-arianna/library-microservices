package mentoring.acomi.bookservice.application.errors;

import lombok.Getter;

@Getter
public class BookRequestAlreadyExists extends RuntimeException {

	private static final long serialVersionUID = 2159978190988655402L;
	
	private final String code = "BOOK_REQUEST_ALREADY_EXIST";
	
	public BookRequestAlreadyExists(String message) {
		super(message);
	}

}
