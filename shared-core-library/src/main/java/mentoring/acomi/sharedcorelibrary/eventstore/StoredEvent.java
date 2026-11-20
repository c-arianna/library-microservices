package mentoring.acomi.sharedcorelibrary.eventstore;

import java.time.Instant;

public interface StoredEvent {
    String eventId();
    Instant occurredAt();
    Object payload();
}
