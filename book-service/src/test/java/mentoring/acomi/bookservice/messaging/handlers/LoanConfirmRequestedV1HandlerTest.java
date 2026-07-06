package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.InvalidEventPayloadException;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.LoanConfirmRequestedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LoanConfirmRequestedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookEventReactor reactor;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

	private LoanConfirmRequestedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new LoanConfirmRequestedV1Handler(reactor, mapper);
	}
	
	@Test
	void shouldHandleLoanConfirmRequestedEvent() {
		
		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();
		
		handler.handleEvent(event);

		CommandLoanEvent command = new CommandLoanEvent(event.payload().loanId(), event.payload().isbn(), event.payload().userId());
		
		verify(reactor, times(1)).handleLoanConfirmRequested(command);
		
	}
	
	@Test
	void shouldRejectPayloadWithBlankLoanId() {
		
		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanConfirmRequestedEvent("", "9788804336327", UUID.randomUUID().toString(), 1);
		
		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(reactor);
		
	}
	
	@Test
	void shouldRejectPayloadWithBlankIsbn() {
		
		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "",
				UUID.randomUUID().toString(), 1);
		
		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(reactor);
		
	}
	
	@Test
	void shouldRejectPayloadWithBlankUserId() {
		
		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "9788804336327", "", 1);
		
		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(reactor);
		
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventTypes eventType() {
		return handler.eventType();
	}

	@Override
	protected IntegrationEventEnvelope<LoanIntegrationPayload> validEvent() {
		return getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CANCELED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanConfirmRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), schemaVersion);
	}
	
	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanConfirmRequestedEvent(String loanId, String isbn, String userId, int schemaVersion) {

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED,
				"test-handler", UUID.randomUUID().toString(), AggregateType.LOAN.name(), 0, Instant.now(),
				schemaVersion, new LoanIntegrationPayload(loanId, isbn, userId));
	}

	
}
