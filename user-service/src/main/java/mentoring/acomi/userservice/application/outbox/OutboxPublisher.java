package mentoring.acomi.userservice.application.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEvent;

@Component
public class OutboxPublisher {

	private final OutboxRepository outboxRepository;
	private final UserEventRepository userEventRepository;
	private final EventDispatcher eventDispatcher;

	private final Logger logger = LogManager.getLogger(OutboxPublisher.class);

	private static final int MAX_RETRY = 50;
	
	public OutboxPublisher(OutboxRepository outboxRepository, UserEventRepository userEventRepository,
			EventDispatcher eventDispatcher) {
		this.outboxRepository = outboxRepository;
		this.userEventRepository = userEventRepository;
		this.eventDispatcher = eventDispatcher;
	}

	@Scheduled(fixedDelay = 5000)
	public void publishPendingEvents() {

		while (true) {
		
			List<OutboxEvent> batch = outboxRepository.findEventsToPublish(Instant.now(), 100);
			
			if (batch.isEmpty()) {
				return;
			}
		
			for (OutboxEvent outbox : batch) {
				try {
					publish(outbox);
				} catch (Exception e) {
					logger.error("[Outbox] publish failed, eventId={}", outbox.eventId(), e);
					handleFailure(outbox, e);
				}
			}
			
			if (batch.size() < 100) {
				return;
			}
		}

	}

	private void publish(OutboxEvent outbox) {
		UserEvent event = userEventRepository.getEventByEventIdAndAggregateType(outbox.eventId(), outbox.aggregateType())
				.orElseThrow(() -> new IllegalStateException("Event not found: %s".formatted(outbox.eventId())));
		eventDispatcher.dispatch(event);
		outboxRepository.published(outbox.eventId(), Instant.now());
	}
	
	private void handleFailure(OutboxEvent outbox, Exception ex) {

		String error = Objects.toString(ex.getMessage(), ex.getClass().getSimpleName());
		
		Instant nextRetryAt = calculateNextRetry(outbox.retryCount());
		
		int nextRetryCount = outbox.retryCount() + 1;
	    OutboxStatus newStatus = nextRetryCount >= MAX_RETRY ? OutboxStatus.FAILED : OutboxStatus.PENDING;
	    Instant retryAt = nextRetryCount >= MAX_RETRY ? null : nextRetryAt;
	        
		outboxRepository.recordFailure(outbox.eventId(), newStatus, error, nextRetryCount, retryAt);
	}

	private Instant calculateNextRetry(int retryCount) {
	    long delaySeconds = Math.min(300, (long) Math.pow(2, retryCount) * 5);
	    return Instant.now().plusSeconds(delaySeconds);
	}
}
