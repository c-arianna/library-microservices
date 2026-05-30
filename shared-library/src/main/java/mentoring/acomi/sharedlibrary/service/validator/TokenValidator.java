package mentoring.acomi.sharedlibrary.service.validator;

import mentoring.acomi.sharedlibrary.model.TokenPrincipal;

public interface TokenValidator {
	
	public TokenPrincipal validateAndExtract(String token);

	public default boolean isValid(String token) {
		try {
			validateAndExtract(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
