package mentoring.acomi.bookservice.domain.errors;

public class BookRequestVoteNotAllowed extends DomainError {

	private static final long serialVersionUID = -4822592820809085044L;

	private static final String code = "BOOK_REQUEST_VOTE_NOT_ALLOWED";
	
	public BookRequestVoteNotAllowed(String message) {
		super(code, message);
	}

}
