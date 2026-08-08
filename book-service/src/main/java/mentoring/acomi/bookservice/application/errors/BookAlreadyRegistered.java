package mentoring.acomi.bookservice.application.errors;

import lombok.Getter;

@Getter
public class BookAlreadyRegistered extends RuntimeException {

	private static final long serialVersionUID = -8900256004619168145L;
	
	private final String code = "BOOK_ALREADY_REGISTERED";
	
	public BookAlreadyRegistered(String message) {
		super(message);
	}

}
