package mentoring.acomi.bookservice.application.projection;

import java.math.BigDecimal;
import java.time.Instant;

import mentoring.acomi.bookservice.application.view.BookRequestView;

public interface BookRequestProjectionOperations {
	 void add(BookRequestView view);
	 void registerVotes(String requestId, int votes, Instant occurredA);
	 void approve(String requestId, Instant occurredA);
	 void reject(String requestId, Instant occurredA);
	 void updatePrice(String requestId, BigDecimal estimatedPrice, Instant occurredA);
}
