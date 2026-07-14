package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanCanceledV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanCanceledV1HandlerTest extends AbstractLoanNotificationHandlerTest {

	private static final String LOAN_ID = UUID.randomUUID().toString();
	
	@Mock
	private LoanProjection projection;
	private LoanCanceledV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanCanceledV1Handler(projection, mapper, notificationService);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_CANCELED;
	}

	@Override
	protected IntegrationEventEnvelope<LoanIntegrationPayload> validEvent() {
		return getLoanCanceledEvent(LOAN_ID, "9788804336327", UUID.randomUUID().toString(), 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(LOAN_ID, IntegrationEventTypes.LOAN_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanCanceledEvent(LOAN_ID, "9788804336327", UUID.randomUUID().toString(), schemaVersion);
	}
	
	@Override
    protected String expectedLoanId() {
        return LOAN_ID;
    }
	
	@Test
	void shouldHandleLoanCanceledEvent() {
		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).cancelLoan(event.payload().loanId(), event.occurredAt());
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", getLoanCanceledEvent("", "9788804336327", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank isbn", "isbn", getLoanCanceledEvent(LOAN_ID, "", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", getLoanCanceledEvent(LOAN_ID, "9788804336327", "", 1)));
	}
			
	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanCanceledEvent(String loanId, String isbn, String userId, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? LOAN_ID : loanId;
		
		return new IntegrationEventEnvelope<>(LOAN_ID, IntegrationEventTypes.LOAN_CANCELED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanIntegrationPayload(loanId, isbn, userId));
	}

}
