package mentoring.acomi.assistantservice.infrastructure.persistence.entity;

import java.time.Instant;

public interface ProjectionEntity {
	Instant getUpdatedAt();
    void markUpdated(Instant updatedAt);
}
