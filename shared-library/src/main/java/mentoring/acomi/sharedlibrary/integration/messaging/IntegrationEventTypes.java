package mentoring.acomi.sharedlibrary.integration.messaging;

import java.util.HashMap;
import java.util.Map;

public enum IntegrationEventTypes {

	BOOK_REGISTERED("book.registered"), BOOK_COPIES_UPDATED("book.copies.updated"), BOOK_RESERVED("book.reserved"),
	BOOK_BORROWED("book.borrowd"), BOOK_RELEASED("book.released"), BOOK_RETURNED("book.returned"),
	BOOK_RESERVATION_REJECTED("book.reservation.rejected"), BOOK_BORROW_REJECTED("book.borrow.rejected"),
	LOAN_REQUESTED("loan.requested"), LOAN_CONFIRMED("loan.confirmed"), LOAN_CANCELED("loan.canceled"), LOAN_RETURNED("loan.returned");

	public final String eventName;

	private IntegrationEventTypes(String eventName) {
		this.eventName = eventName;
	}

	private static final Map<String, IntegrationEventTypes> names = new HashMap<>();

	static {
		for (IntegrationEventTypes name : values()) {
			names.put(name.eventName, name);
		}
	}

	public static IntegrationEventTypes valueOfLabel(String eventName) {
		return names.get(eventName);
	}
	
	@Override 
	public String toString() { 
	    return this.eventName; 
	}
}
