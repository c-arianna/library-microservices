package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanFailedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanFailedV1HandlerTest extends AbstractLoanNotificationHandlerTest {

	private static final String LOAN_ID = UUID.randomUUID().toString();
	
	@Mock
	private LoanProjection projection;	
	private LoanFailedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanFailedV1Handler(projection, mapper, notificationService);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<LoanFailedIntegrationPayload> validEvent() {
		return getLoanFailedEvent(LOAN_ID, "FAILED", 1);
	}

	@Override
    protected String expectedLoanId() {
        return LOAN_ID;
    }
	
	@Test
	void shouldHandleLoanFailedEvent() {
		IntegrationEventEnvelope<LoanFailedIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).failLoan(event.payload().loanId(), event.occurredAt());
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", getLoanFailedEvent("", "Failed", 1)));
	}
	
	private IntegrationEventEnvelope<LoanFailedIntegrationPayload> getLoanFailedEvent(String loanId, String reason, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_FAILED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanFailedIntegrationPayload(loanId, reason));
	}

}
