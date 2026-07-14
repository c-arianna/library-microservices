package mentoring.acomi.bookservice.messaging.handlers;

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

import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.LoanCanceledV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanCanceledV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookEventReactor reactor;
	private LoanCanceledV1Handler handler;
	
	@BeforeEach
	void setUp() {
		handler = new LoanCanceledV1Handler(reactor, mapper);
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
		return getLoanCanceledEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanCanceledEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), schemaVersion);
	}
	
	@Test
	void shouldHandleLoanCanceledEvent() {
		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();
		handler.handleEvent(event);

		CommandLoanEvent command = new CommandLoanEvent(event.payload().loanId(), event.payload().isbn(), event.payload().userId());
		verify(reactor, times(1)).handleLoanCanceled(command);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), reactor))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank loanId", "loanId", getLoanCanceledEvent("", "9788804336327", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank isbn", "isbn", 
	                	   getLoanCanceledEvent(UUID.randomUUID().toString(), "", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", getLoanCanceledEvent(UUID.randomUUID().toString(), "9788804336327", "", 1)));
	}
		
	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanCanceledEvent(String loanId, String isbn, String userId, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CANCELED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanIntegrationPayload(loanId, isbn, userId));
	}

}
