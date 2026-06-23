package mentoring.acomi.sharedlibrary.eventstore;

import java.time.Instant;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String aggregateId;
    
    protected String aggregateType;

    protected String eventId;

    protected String eventType;

    protected int eventVersion;
    
    protected String eventCategory;
    
    protected boolean processed = false;
    
    protected Integer schemaVersion;
        
	protected Instant  occurredAt;
}
