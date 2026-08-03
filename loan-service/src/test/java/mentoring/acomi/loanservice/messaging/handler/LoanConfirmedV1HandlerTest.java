package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanConfirmedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanConfirmedV1HandlerTest extends AbstractEventHandlerTest {
	
	@Mock
	private ProjectionDispatcher dispatcher;
	private LoanConfirmedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanConfirmedV1Handler(dispatcher, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<LoanIntegrationPayload> validEvent() {
		return getLoanConfirmedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 1);
	}
	
	@Test
	void shouldHandleLoanConfirmedEvent() {
		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(dispatcher).dispatch(event, event.payload());
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), dispatcher))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", getLoanConfirmedEvent("", "9788804336327", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank isbn", "isbn", getLoanConfirmedEvent(UUID.randomUUID().toString(), "", 
	                		   UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", getLoanConfirmedEvent(UUID.randomUUID().toString(), "9788804336327", "", 1)));
	}
		
	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanConfirmedEvent(String loanId, String isbn, String userId, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRMED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanIntegrationPayload(loanId, isbn, userId));
	}

}
