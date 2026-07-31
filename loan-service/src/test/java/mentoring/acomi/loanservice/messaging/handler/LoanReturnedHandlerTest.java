package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import mentoring.acomi.loanservice.application.projection.UserLoanStatisticProjectionOperations;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanReturnedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanReturnedHandlerTest extends AbstractEventHandlerTest {
	
	@Mock
	private LoanProjectionOperations projectionOperations;
	
	@Mock
	private UserLoanStatisticProjectionOperations statisticProjectionOperations;
	
	@Mock
	private DailyLoanStatisticProjectionOperations dailyStatisticOperation;
	
	private LoanReturnedHandler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanReturnedHandler(projectionOperations, statisticProjectionOperations, dailyStatisticOperation, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<LoanReturnedIntegrationPayload> validEvent() {
		return getLoanReturnedEvent1(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString());
	}
	
	@Test
	void shouldHandleLoanReturnedEvent() {
		IntegrationEventEnvelope<LoanReturnedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		LocalDate returnedAt = event.occurredAt().atZone(ZoneOffset.UTC).toLocalDate();
		verify(projectionOperations, times(1)).returnLoan(event.payload().loanId(), event.occurredAt(), returnedAt);
		verify(statisticProjectionOperations, times(1)).registerOverdueLoan(event.payload().userId(), event.payload().loanId(), returnedAt);
		
		LocalDate statisticDate = event.occurredAt().atZone(ZoneId.of("Europe/Rome")).toLocalDate();
		verify(dailyStatisticOperation, times(1)).registerLoanReturned(statisticDate);
	}
	
	@Test
	void shouldHandleVersion2Event() {
		IntegrationEventEnvelope<LoanReturnedIntegrationPayload> event = getLoanReturnedEvent(UUID.randomUUID().toString(), "9788804336327", 
				UUID.randomUUID().toString(), LocalDate.now(), 2);
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(projectionOperations, times(1)).returnLoan(event.payload().loanId(), event.occurredAt(), event.payload().returnedAt());
		verify(statisticProjectionOperations, times(1)).registerOverdueLoan(event.payload().userId(), event.payload().loanId(), event.payload().returnedAt());
	}

	@Test
	void shouldRejectVersion2WithoutReturnedAt() {
		
		IntegrationEventEnvelope<LoanReturnedIntegrationPayload> event = getLoanReturnedEvent(UUID.randomUUID().toString(), "9788804336327", 
				UUID.randomUUID().toString(), null, 2);
		
		Assertions.assertThrows(IllegalStateException.class, () -> handler.handleEvent(event));
		
		verifyNoInteractions(projectionOperations);
		
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", getLoanReturnedEvent1("", "9788804336327", UUID.randomUUID().toString())),
	                   new InvalidPayloadScenario("blank isbn", "isbn", getLoanReturnedEvent1(UUID.randomUUID().toString(), "", 
	                		   UUID.randomUUID().toString())),
	                   new InvalidPayloadScenario("blank userId", "userId", getLoanReturnedEvent1(UUID.randomUUID().toString(), "9788804336327","")));
	}
	
	private IntegrationEventEnvelope<LoanReturnedIntegrationPayload> getLoanReturnedEvent1(String loanId, String isbn, String userId) {
		return getLoanReturnedEvent(loanId, isbn, userId, null, 1);
	}
	
	private IntegrationEventEnvelope<LoanReturnedIntegrationPayload> getLoanReturnedEvent(String loanId, String isbn, String userId, 
			LocalDate returnedAt, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_RETURNED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanReturnedIntegrationPayload(loanId, isbn, userId, returnedAt));
	}

}
