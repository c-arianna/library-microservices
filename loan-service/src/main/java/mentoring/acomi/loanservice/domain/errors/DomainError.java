package mentoring.acomi.loanservice.domain.errors;

import lombok.Getter;

@Getter
public class DomainError extends RuntimeException {

	private static final long serialVersionUID = -6056907041379629736L;
	
	private String code;
	
	public DomainError(String code, String message) {
		super(message);
		this.code = code;
	}

}
