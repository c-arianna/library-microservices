package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.time.LocalDate;
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
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.LoanRequestedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LoanRequestedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookEventReactor reactor;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

	private LoanRequestedV1Handler handler;
	
	private LocalDate start = LocalDate.now();
	private LocalDate end = start.plusDays(30);
	
	@BeforeEach
	void setUp() {
		mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new LoanRequestedV1Handler(reactor, mapper);
	}
	
	@Test
	void shouldHandleLoanRequestedEvent() {
		
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = validEvent();
		
		handler.handleEvent(event);

		CommandLoanEvent command = new CommandLoanEvent(event.payload().loanId(), event.payload().isbn(), event.payload().userId());
		
		verify(reactor, times(1)).handleLoanRequested(command);
		
	}
	
	@Test
	void shouldRejectPayloadWithBlankLoanId() {
		
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = getLoanRequestedEvent("", "9788804336327", UUID.randomUUID().toString(), 
				start, end, 1);
		
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
		
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = getLoanRequestedEvent(UUID.randomUUID().toString(), "",
				UUID.randomUUID().toString(), start, end, 1);
		
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
		
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", "", 
				start, end, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );
		
	}
	
	@Test
	void shouldRejectPayloadWithoutStartDate() {
		
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", 
				UUID.randomUUID().toString(), null, end, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("start"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(reactor)
	    );
		
	}
	
	@Test
	void shouldRejectPayloadWithoutEndDate() {
		
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", 
				UUID.randomUUID().toString(), start, null, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("end"),
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
	protected IntegrationEventEnvelope<LoanRequestedIntegrationPayload> validEvent() {
		return getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), start, end, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CANCELED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanRequestedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), start, end, schemaVersion);
	}
	
	private IntegrationEventEnvelope<LoanRequestedIntegrationPayload> getLoanRequestedEvent(String loanId, String isbn, String userId, 
			LocalDate start, LocalDate end, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_REQUESTED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(),
				schemaVersion, new LoanRequestedIntegrationPayload(loanId, isbn, userId, start, end));
	}

	
}
