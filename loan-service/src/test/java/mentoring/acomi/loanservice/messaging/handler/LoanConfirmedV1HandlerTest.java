package mentoring.acomi.loanservice.messaging.handler;

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
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanConfirmedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LoanConfirmedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private LoanProjection projection;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

	@Mock
	private LoanNotificationService notificationService;
	
	private LoanConfirmedV1Handler handler;

	@BeforeEach
	void setUp() {
        mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new LoanConfirmedV1Handler(projection, mapper, notificationService);
	}

	@Test
	void shouldHandleLoanConfirmedEvent() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = validEvent();

		handler.handleEvent(event);

		verify(projection, times(1)).confirmLoan(event.payload().loanId(), event.occurredAt());

	}

	@Test
	void shouldRejectPayloadWithBlankLoanId() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanConfirmedEvent("", "9788804336327", UUID.randomUUID().toString(), 1);

		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("loanId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );

	}
	
	@Test
	void shouldRejectPayloadWithBlankIsbn() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanConfirmedEvent(UUID.randomUUID().toString(), "", 
				UUID.randomUUID().toString(), 1);

		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("isbn"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithBlankUserId() {

		IntegrationEventEnvelope<LoanIntegrationPayload> event = getLoanConfirmedEvent(UUID.randomUUID().toString(), "9788804336327", "", 1);

		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
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
		return getLoanConfirmedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanConfirmedEvent(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), schemaVersion);
	}

	private IntegrationEventEnvelope<LoanIntegrationPayload> getLoanConfirmedEvent(String loanId, String isbn, String userId, int schemaVersion) {

		String aggregateId = loanId == null || loanId.isBlank() ? UUID.randomUUID().toString() : loanId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRMED,
				"test-handler", aggregateId, AggregateType.LOAN.name(), 0, Instant.now(), schemaVersion, 
				new LoanIntegrationPayload(loanId, isbn, userId));
	}

}
