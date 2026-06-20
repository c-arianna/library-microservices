package mentoring.acomi.sharedlibrary.eventstore;

import java.time.Instant;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class BaseEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String aggregateId;

    protected String eventId;

    protected String eventType;

    protected int eventVersion;
    
	protected Instant  occurredAt;
}
