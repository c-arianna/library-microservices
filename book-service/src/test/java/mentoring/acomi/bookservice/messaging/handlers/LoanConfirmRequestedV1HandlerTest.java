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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.LoanConfirmRequestedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanConfirmRequestedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookEventReactor reactor;
	private LoanConfirmRequestedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new LoanConfirmRequestedV1Handler(reactor, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<LoanIntegrationPayload> validEvent() {
		return getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 1);
	}

	@Test
	void shouldHandleLoanConfirmRequestedEvent() {
		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		
		Assertions.assertTrue(notification.isEmpty());
		
		CommandLoanEvent command = new CommandLoanEvent(event.payload().loanId(), event.payload().isbn(), event.payload().userId());
		verify(reactor, times(1)).handleLoanConfirmRequested(command);
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), reactor))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", 
	    		           getLoanConfirmRequestedEvent("", "9788804336327", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank isbn", "isbn", 
	                	   getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", 
	                	   getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "9788804336327", "", 1)));
	}
	
	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanConfirmRequestedEvent(String loanId, String isbn,
			String userId, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
				IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, "test-handler", aggregateId, AggregateType.LOAN.name(), 0,
				Instant.now(), schemaVersion, new LoanIntegrationPayload(loanId, isbn, userId));
	}

}
