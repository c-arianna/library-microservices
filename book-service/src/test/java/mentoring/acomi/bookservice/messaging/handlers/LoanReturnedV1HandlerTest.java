package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
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
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.LoanReturnedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LoanReturnedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookEventReactor reactor;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	private EventPayloadMapper mapper;

	private LoanReturnedV1Handler handler;

	@BeforeEach
	void setUp() {
        mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new LoanReturnedV1Handler(reactor, mapper);
	}

	@Test
	void shouldHandleLoanReturnedEvent() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();

		handler.handleEvent(event);

		CommandLoanEvent command = new CommandLoanEvent(event.payload().loanId(), event.payload().isbn(),
				event.payload().userId());

		verify(reactor, times(1)).handleLoanReturned(command);

	}

	@Test
	void shouldRejectPayloadWithBlankLoanId() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanReturnedEvent("", "9788804336327",
				UUID.randomUUID().toString(), 1);

		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("loanId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );

	}

	@Test
	void shouldRejectPayloadWithBlankIsbn() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanReturnedEvent(UUID.randomUUID().toString(), "",
				UUID.randomUUID().toString(), 1);

		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("isbn"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );

	}

	@Test
	void shouldRejectPayloadWithBlankUserId() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanReturnedEvent(UUID.randomUUID().toString(),
				"9788804336327", "", 1);

		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );

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
		return getLoanReturnedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CANCELED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanReturnedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(),
				schemaVersion);
	}

	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanReturnedEvent(String loanId, String isbn,
			String userId, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_RETURNED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanIntegrationPayload(loanId, isbn, userId));
	}

}
