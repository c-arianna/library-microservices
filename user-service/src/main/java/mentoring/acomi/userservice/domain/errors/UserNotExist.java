package mentoring.acomi.userservice.domain.errors;

public class UserNotExist extends DomainError{

	private static final long serialVersionUID = 6817599286997595676L;

	private static final String code = "USER_NOT_CREATED";
	
	public UserNotExist(String message) {
		super(code, message);
	}

}
