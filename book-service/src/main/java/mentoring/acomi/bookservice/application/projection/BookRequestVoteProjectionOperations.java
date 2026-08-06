package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

public interface BookRequestVoteProjectionOperations {
	void add(String requestId, String userId, Instant occurredAt);
}
