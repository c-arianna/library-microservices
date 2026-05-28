package mentoring.acomi.bookservice.domain.errors;

public class CannotRemoveBookCopies extends DomainError {

	private static final long serialVersionUID = -8824584403469222016L;

	private static final String code = "CANNOT_REMOVE_BOOK_COPIES";
	
	public CannotRemoveBookCopies(String message) {
		super(code, message);
	}

}
