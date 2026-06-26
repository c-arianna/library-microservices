package mentoring.acomi.bookservice.domain.errors;

public class ReservationMissing extends DomainError {

	private static final long serialVersionUID = -8824584403469222016L;

	private static final String code = "RESERVATION_MISSING";
	
	public ReservationMissing(String message) {
		super(code, message);
	}

}