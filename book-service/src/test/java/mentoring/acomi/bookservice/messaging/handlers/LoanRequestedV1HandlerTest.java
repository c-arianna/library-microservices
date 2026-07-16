package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
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

import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.LoanRequestedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanRequestedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookEventReactor reactor;
	private LoanRequestedV1Handler handler;
	
	private LocalDate start = LocalDate.now();
	private LocalDate end = start.plusDays(30);
	
	@BeforeEach
	void setUp() {
		handler = new LoanRequestedV1Handler(reactor, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<LoanRequestedIntegrationPayload> validEvent() {
		return getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), start, end, 1);
	}
	
	@Test
	void shouldHandleLoanRequestedEvent() {
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		
		CommandLoanEvent command = new CommandLoanEvent(event.payload().loanId(), event.payload().isbn(), event.payload().userId());
		verify(reactor, times(1)).handleLoanRequested(command);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), reactor))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", 
	    		           getLoanRequestedEvent("", "9788804336327", UUID.randomUUID().toString(), start, end, 1)),
	                   new InvalidPayloadScenario("blank isbn", "isbn", 
	                	   getLoanRequestedEvent(UUID.randomUUID().toString(), "",  UUID.randomUUID().toString(), start, end, 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", 
	                	   getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", "", start, end, 1)),
	                   new InvalidPayloadScenario("null start date", "start", 
	                	   getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), null, end, 1)),
	                   new InvalidPayloadScenario("null end date", "end", 
	                		   getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), start, null, 1)));
	}
				
	private IntegrationEventEnvelope<LoanRequestedIntegrationPayload> getLoanRequestedEvent(String loanId, String isbn, String userId, 
			LocalDate start, LocalDate end, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_REQUESTED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(),
				schemaVersion, new LoanRequestedIntegrationPayload(loanId, isbn, userId, start, end));
	}

}