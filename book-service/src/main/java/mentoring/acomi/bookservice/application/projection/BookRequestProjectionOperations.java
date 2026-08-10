package mentoring.acomi.bookservice.application.projection;

import java.math.BigDecimal;
import java.time.Instant;

import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestAddedIntegrationPayload;

public interface BookRequestProjectionOperations {
	 void add(BookRequestAddedIntegrationPayload payload, Instant occurredAt);
	 void registerVotes(String requestId, int votes, Instant occurredA);
	 void approve(String requestId, Instant occurredA);
	 void reject(String requestId, Instant occurredA);
	 void updatePrice(String requestId, BigDecimal estimatedPrice, Instant occurredA);
}
