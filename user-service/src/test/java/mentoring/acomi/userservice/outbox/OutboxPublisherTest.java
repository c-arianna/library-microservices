package mentoring.acomi.userservice.outbox;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.outbox.OutboxPublisher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;

@ExtendWith(MockitoExtension.class)
public class OutboxPublisherTest {

	@Mock
	private OutboxRepository outboxRepository;

	@Mock
	private UserEventRepository eventRepository;

	@Mock
	private EventDispatcher eventDispatcher;

	@InjectMocks
	private OutboxPublisher publisher;

	@Test
	void shouldPublishEvent() {

		String userId = UUID.randomUUID().toString();
		String eventId = userId;

		OutboxEvent outbox = getPendingOutboxEvent(eventId);

		UserUnsubscribeEvent event = createUserUnsubscribeEvent(userId, eventId);

		when(outboxRepository.findEventsToPublish(any(Instant.class), eq(100))).thenReturn(List.of(outbox));

		when(eventRepository.getEventByEventIdAndAggregateType(eventId, "USER")).thenReturn(Optional.of(event));

		publisher.publishPendingEvents();

		verify(eventDispatcher).dispatch(event);

		verify(outboxRepository).published(eq(eventId), any(Instant.class));

	}

	@Test
	void shouldIncrementRetryWhenPublishFails() {

		String userId = UUID.randomUUID().toString();
		String eventId = UUID.randomUUID().toString();

		OutboxEvent outbox = getPendingOutboxEvent(eventId);

		UserUnsubscribeEvent event = createUserUnsubscribeEvent(userId, eventId);

		when(outboxRepository.findEventsToPublish(any(Instant.class), eq(100))).thenReturn(List.of(outbox));

		when(eventRepository.getEventByEventIdAndAggregateType(eventId, "USER")).thenReturn(Optional.of(event));

		doThrow(new RuntimeException("Connection refused")).when(eventDispatcher).dispatch(event);

		publisher.publishPendingEvents();

		verify(outboxRepository).recordFailure(eq(eventId), eq(OutboxStatus.PENDING), eq("Connection refused"), eq(1),
				any(Instant.class));

	}

	@Test
	void shouldMarkEventAsFailedWhenMaxRetryReached() {

		String userId = UUID.randomUUID().toString();
		String eventId = UUID.randomUUID().toString();

		OutboxEvent outbox = new OutboxEvent(eventId, "USER", OutboxStatus.PENDING, 49, null, Instant.now(), null,
				null);

		UserUnsubscribeEvent event = createUserUnsubscribeEvent(userId, eventId);

		when(outboxRepository.findEventsToPublish(any(Instant.class), eq(100))).thenReturn(List.of(outbox));

		when(eventRepository.getEventByEventIdAndAggregateType(eventId, "USER")).thenReturn(Optional.of(event));

		doThrow(new RuntimeException("RabbitMQ unavailable")).when(eventDispatcher).dispatch(event);

		publisher.publishPendingEvents();

		verify(outboxRepository).recordFailure(eq(eventId), eq(OutboxStatus.FAILED), eq("RabbitMQ unavailable"), eq(50),
				isNull());

	}

	@Test
	void shouldHandleMissingEvent() {

		String eventId = UUID.randomUUID().toString();

		OutboxEvent outbox = getPendingOutboxEvent(eventId);

		when(outboxRepository.findEventsToPublish(any(Instant.class), eq(100))).thenReturn(List.of(outbox));

		when(eventRepository.getEventByEventIdAndAggregateType(eventId, "USER")).thenReturn(Optional.empty());

		publisher.publishPendingEvents();

		verify(outboxRepository).recordFailure(eq(eventId), eq(OutboxStatus.PENDING), contains("Event not found"),
				eq(1), any(Instant.class));

	}

	private UserUnsubscribeEvent createUserUnsubscribeEvent(String userId, String eventId) {
		UserUnsubscribedPayload payload = new UserUnsubscribedPayload(userId, "test@gmail.com", "");
		return new UserUnsubscribeEvent(userId, eventId, 0, payload, Instant.now());
	}

	private OutboxEvent getPendingOutboxEvent(String eventId) {
		return new OutboxEvent(eventId, "USER", OutboxStatus.PENDING, 0, null, Instant.now(), null, null);
	}

}