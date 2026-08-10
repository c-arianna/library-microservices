package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestPriceUpdatedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestPriceUpdatedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@ExtendWith(MockitoExtension.class)
public class BookRequestPriceUpdatedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookRequestProjectionOperations projectionOperations;
	
	private BookRequestPriceUpdatedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		handler = new BookRequestPriceUpdatedV1Handler(projectionOperations, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<BookRequestPriceUpdatedIntegrationPayload> validEvent() {
		return getBookRequestPriceUpdatedEvent(UUID.randomUUID().toString(), BigDecimal.valueOf(25.9), 1);
	}
	
	void shouldHandleBookRequestPriceUpdatedEvent() {
		IntegrationEventEnvelope<BookRequestPriceUpdatedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(projectionOperations, times(1)).updatePrice(event.payload().requestId(),event.payload().estimatedPrice(), event.occurredAt());
	}
	
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank requestId", "requestId", 
	    					getBookRequestPriceUpdatedEvent("", BigDecimal.valueOf(25.9), 1)),
	    			   new InvalidPayloadScenario("null estimatedPrice", "estimatedPrice", 
	    	    		getBookRequestPriceUpdatedEvent(UUID.randomUUID().toString(), null, 1)));
	}

	private IntegrationEventEnvelope<BookRequestPriceUpdatedIntegrationPayload> getBookRequestPriceUpdatedEvent(String requestId, 
			BigDecimal estimatedPrice, int schemaVersion) {

		String aggregateId = requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_REQUEST_PRICE_UPDATED,
				"test-handler", aggregateId, AggregateType.BOOK_REQUEST.name(), 0, Instant.now(),
				schemaVersion, new BookRequestPriceUpdatedIntegrationPayload(requestId, estimatedPrice));
	}
}
