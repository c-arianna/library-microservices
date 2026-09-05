package mentoring.acomi.sharedcorelibrary.eventstore.replay;

import java.time.Instant;

public record ReplayRequestedEvent(String targetService, Instant occurredAt) {}
