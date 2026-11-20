package mentoring.acomi.sharedcorelibrary.eventstore;

public interface IntegrationEvent extends StoredEvent {
	String aggregateId();
	String aggregateType();
	int eventVersion();
}
