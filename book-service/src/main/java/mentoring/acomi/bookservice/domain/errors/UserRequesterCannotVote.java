package mentoring.acomi.bookservice.domain.errors;

public class UserRequesterCannotVote extends DomainError {

	private static final long serialVersionUID = -4364567003771534281L;
	
	private static final String code = "USER_REQUESTER_CANNOT_VOTE";
	
	public UserRequesterCannotVote(String message) {
		super(code, message);
	}

}
