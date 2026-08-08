package mentoring.acomi.bookservice.domain.errors;

public class UserAlreadyVoted extends DomainError {

	private static final long serialVersionUID = 5705208016963893360L;
	
	private static final String code = "USER_ALREADY_VOTED";
	
	public UserAlreadyVoted(String message) {
		super(code, message);
	}

}
