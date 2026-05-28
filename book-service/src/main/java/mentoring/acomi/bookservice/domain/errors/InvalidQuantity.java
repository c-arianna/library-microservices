package mentoring.acomi.bookservice.domain.errors;

public class InvalidQuantity extends DomainError {

	private static final long serialVersionUID = 1L;
	
	private static final String code = "INVALID_BOOK_COPY_QUANTITY";
	
	public InvalidQuantity(String message) {
		super(code, message);
	}

}
