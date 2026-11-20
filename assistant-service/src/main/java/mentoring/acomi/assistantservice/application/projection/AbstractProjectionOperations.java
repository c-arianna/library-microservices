package mentoring.acomi.assistantservice.application.projection;

import java.time.Instant;

import mentoring.acomi.assistantservice.application.repositories.ViewRepository;

public abstract class AbstractProjectionOperations<T> implements ProjectionOperations<T> {

	protected final ViewRepository<T> repository;
	
	protected AbstractProjectionOperations(ViewRepository<T> repository) {
        this.repository = repository;
    }
	
	@Override
	public void upsert(T view, Instant eventOccurredAt) {
		repository.upsert(view, eventOccurredAt);
	}

}
