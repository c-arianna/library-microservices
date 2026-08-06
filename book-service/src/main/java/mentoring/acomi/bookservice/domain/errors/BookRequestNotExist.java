package mentoring.acomi.bookservice.domain.errors;

public class BookRequestNotExist extends DomainError {

	private static final long serialVersionUID = 8547059347965697024L;

	private static final String code = "BOOK_REQUEST_NOT_EXIST";
	
	public BookRequestNotExist(String message) {
		super(code, message);
	}

}
