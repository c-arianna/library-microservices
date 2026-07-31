package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

import mentoring.acomi.loanservice.application.projection.DailyLoanStatisticProjectionOperations;
import mentoring.acomi.loanservice.application.projection.LoanProjectionOperations;
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
	private LoanProjectionOperations projectionOperations;
	
	@Mock
	private DailyLoanStatisticProjectionOperations dailyStatisticOperation;
	
	private LoanConfirmedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanConfirmedV1Handler(projectionOperations, dailyStatisticOperation, mapper);
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
		verify(projectionOperations, times(1)).confirmLoan(event.payload().loanId(), event.occurredAt());
		
		LocalDate statisticDate = event.occurredAt().atZone(ZoneId.of("Europe/Rome")).toLocalDate();
		verify(dailyStatisticOperation, times(1)).registerLoanConfirmed(statisticDate);
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
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
