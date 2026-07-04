package mentoring.acomi.sharedcodelibrary.eventstore.replay;

import java.time.Instant;

public record ReplayRequestedEvent(String targetService, Instant occurredAt) {}
