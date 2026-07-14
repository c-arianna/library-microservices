package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
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
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanRequestedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanRequestedV1HandlerTest extends AbstractLoanNotificationHandlerTest {

	private static final String LOAN_ID = UUID.randomUUID().toString();
	
	@Mock
	private LoanProjection projection;
	private LoanRequestedV1Handler handler;

	private LocalDate start = LocalDate.now();
	private LocalDate end = start.plusDays(30);

	@BeforeEach
	void setUp() {
		handler = new LoanRequestedV1Handler(projection, mapper, notificationService);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_REQUESTED;
	}

	@Override
	protected IntegrationEventEnvelope<LoanRequestedIntegrationPayload> validEvent() {
		return getLoanRequestedEvent(LOAN_ID, "9788804336327", UUID.randomUUID().toString(), start, end, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanRequestedEvent(LOAN_ID, "9788804336327", UUID.randomUUID().toString(), start, end, schemaVersion);
	}
	
	@Override
    protected String expectedLoanId() {
        return LOAN_ID;
    }
	
	@Test
	void shouldHandleLoanRequestedEvent() {
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).loanInsert(event.payload(), event.occurredAt());
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", 
	    		           getLoanRequestedEvent("", "9788804336327", UUID.randomUUID().toString(), start, end, 1)),
	                   new InvalidPayloadScenario("blank isbn", "isbn", getLoanRequestedEvent(LOAN_ID, "", UUID.randomUUID().toString(), start, end, 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", getLoanRequestedEvent(LOAN_ID, "9788804336327", "", start, end, 1)),
	                   new InvalidPayloadScenario("null start date", "start", 
	                	   getLoanRequestedEvent(LOAN_ID, "9788804336327", UUID.randomUUID().toString(), null, end, 1)),
	                   new InvalidPayloadScenario("null end date", "end", 
	                	   getLoanRequestedEvent(LOAN_ID, "9788804336327", UUID.randomUUID().toString(), start, null, 1)));
	}
	
	private IntegrationEventEnvelope<LoanRequestedIntegrationPayload> getLoanRequestedEvent(String loanId, String isbn,
			String userId, LocalDate start, LocalDate end, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_REQUESTED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanRequestedIntegrationPayload(loanId, isbn, userId, start, end));
	}

}
