package mentoring.acomi.loanservice.messaging.handler;

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
import mentoring.acomi.loanservice.application.errors.InvalidEventPayloadException;
import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanFailedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LoanFailedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private LoanProjection projection;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

	private LoanFailedV1Handler handler;

	@BeforeEach
	void setUp() {
        mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new LoanFailedV1Handler(projection, mapper);
	}

	@Test
	void shouldHandleLoanFailedEvent() {

		IntegrationEventEnvelope<LoanFailedIntegrationPayload> event = validEvent();

		handler.handleEvent(event);

		verify(projection, times(1)).failLoan(event.payload().loanId(), event.occurredAt());

	}

	@Test
	void shouldRejectPayloadWithBlankLoanId() {

		IntegrationEventEnvelope<LoanFailedIntegrationPayload> event = getLoanFailedEvent("", "Failed", 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);

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
	protected IntegrationEventEnvelope<LoanFailedIntegrationPayload> validEvent() {
		return getLoanFailedEvent(UUID.randomUUID().toString(), "FAILED", 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getLoanFailedEvent(UUID.randomUUID().toString(), "FAILED", schemaVersion);
	}

	private IntegrationEventEnvelope<LoanFailedIntegrationPayload> getLoanFailedEvent(String loanId, String reason, int schemaVersion) {

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_FAILED,
				"test-handler", UUID.randomUUID().toString(), AggregateType.LOAN.name(), 0, Instant.now(),
				schemaVersion, new LoanFailedIntegrationPayload(loanId, reason));
	}

}
