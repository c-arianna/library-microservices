package mentoring.acomi.bookservice.domain.errors;

public class BookNotRegistered extends DomainError {

	private static final long serialVersionUID = -3085803485844983199L;

	private static final String code = "BOOK_NOT_REGISTERED";
	
	public BookNotRegistered(String message) {
		super(code, message);
	}

}
