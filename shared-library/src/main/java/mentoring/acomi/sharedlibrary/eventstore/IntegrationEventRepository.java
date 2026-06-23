package mentoring.acomi.sharedlibrary.eventstore;

public interface IntegrationEventRepository<E extends IntegrationEvent>{
	public void save(E event);
	public boolean exists(String eventId, String aggregateType);
	
}
