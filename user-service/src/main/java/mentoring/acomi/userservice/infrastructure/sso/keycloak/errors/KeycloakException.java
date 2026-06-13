package mentoring.acomi.userservice.infrastructure.sso.keycloak.errors;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public class KeycloakException extends RuntimeException{

	private static final long serialVersionUID = 3845004796998387597L;
	
	private HttpStatusCode httpStatusCode;
	
	public KeycloakException(HttpStatusCode httpStatusCode, String message) {
		super(message);
		this.httpStatusCode = httpStatusCode;		
	}

}
