package mentoring.acomi.userservice.application.errors;

public class IdentityProviderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final IdentityProviderError error;

    public IdentityProviderException(IdentityProviderError error, String message) {
        super(message);
        this.error = error;
    }

    public IdentityProviderError error() {
        return error;
    }

}
