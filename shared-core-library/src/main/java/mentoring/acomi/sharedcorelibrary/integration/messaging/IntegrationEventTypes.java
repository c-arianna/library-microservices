package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.util.HashMap;
import java.util.Map;

public enum IntegrationEventTypes {

	BOOK_REGISTERED("book.registered"), BOOK_COPIES_UPDATED("book.copies.updated"), BOOK_RESERVED("book.reserved"),
	BOOK_BORROWED("book.borrowed"), BOOK_RELEASED("book.released"), BOOK_RETURNED("book.returned"),
	BOOK_RESERVATION_REJECTED("book.reservation.rejected"), BOOK_BORROW_REJECTED("book.borrow.rejected"),
	LOAN_REQUESTED("loan.requested"), LOAN_CONFIRMED("loan.confirmed"), LOAN_CONFIRM_REQUESTED("loan.confirm.requested"), LOAN_CANCELED("loan.canceled"), 
	LOAN_RETURNED("loan.returned"), LOAN_RESERVED("loan.reserved"), LOAN_FAILED("loan.failed"),
	USER_SUBSCRIBED("user.subscribed"), USER_UNSUBSCRIBED("user.unsubscribed"), USER_SUSPENDED("user.suspended"), 
	USER_UNSUSPENDED("user.unsuspended"), LIBRARY_CARD_ASSIGNED("user.libraryCard.assigned");

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

	public String getRoutingKey() { 
	    return this.eventName; 
	}
}
