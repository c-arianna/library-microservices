package mentoring.acomi.bookservice.domain.errors;

public class BookRequestAlreadyClosed extends DomainError {

	private static final long serialVersionUID = -4822592820809085044L;

	private static final String code = "BOOK_REQUEST_ALREADY_CLOSED";
	
	public BookRequestAlreadyClosed(String message) {
		super(code, message);
	}

}
