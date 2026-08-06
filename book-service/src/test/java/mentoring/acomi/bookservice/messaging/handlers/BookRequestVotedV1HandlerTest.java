package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.bookservice.application.projection.BookRequestProjectionOperations;
import mentoring.acomi.bookservice.application.projection.BookRequestVoteProjectionOperations;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestVotedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestVotedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@ExtendWith(MockitoExtension.class)
public class BookRequestVotedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookRequestProjectionOperations projectionOperations;
	
	@Mock
	private BookRequestVoteProjectionOperations voteProjectionOperations;
	
	private BookRequestVotedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		handler = new BookRequestVotedV1Handler(projectionOperations, voteProjectionOperations, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<BookRequestVotedIntegrationPayload> validEvent() {
		return getBookRequestVotedEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1);
	}
	
	void shouldHandleBookRequestVotedEvent() {
		IntegrationEventEnvelope<BookRequestVotedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(projectionOperations, times(1)).registerVotes(event.payload().requestId(), 1, event.occurredAt());
		verify(voteProjectionOperations, times(1)).add(event.payload().requestId(), event.payload().userId(), event.occurredAt());
	}
	
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank requestId", "requestId", 
	    		       getBookRequestVotedEvent("", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", 
	                		   getBookRequestVotedEvent(UUID.randomUUID().toString(), "", 1)));
	}

	private IntegrationEventEnvelope<BookRequestVotedIntegrationPayload> getBookRequestVotedEvent(String requestId, String userId, 
			int schemaVersion) {

		String aggregateId = requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_REQUEST_VOTED,
				"test-handler", aggregateId, AggregateType.BOOK_REQUEST.name(), 0, Instant.now(),
				schemaVersion, new BookRequestVotedIntegrationPayload(requestId, userId));
	}
}
