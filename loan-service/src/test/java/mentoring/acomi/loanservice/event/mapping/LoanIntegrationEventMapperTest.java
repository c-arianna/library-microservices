package mentoring.acomi.loanservice.event.mapping;

import java.time.Instant;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;
import mentoring.acomi.loanservice.domain.events.payload.LoanFailedPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.LoanIntegrationEventMapper;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

public class LoanIntegrationEventMapperTest {

	private static final String LOAN_FAILED_EVENT_NAME = IntegrationEventTypes.LOAN_FAILED.eventName;
	private static final String LOAN_RESERVED_EVENT_NAME = IntegrationEventTypes.LOAN_RESERVED.eventName;
	private static final String LOAN_RETURNED_EVENT_NAME = IntegrationEventTypes.LOAN_RETURNED.eventName;
	private static final String LOAN_CANCELED_EVENT_NAME = IntegrationEventTypes.LOAN_CANCELED.eventName;
	private static final String LOAN_CONFIRM_REQUESTED_EVENT_NAME = IntegrationEventTypes.LOAN_CONFIRM_REQUESTED.eventName;
	private static final String LOAN_CONFIRMED_EVENT_NAME = IntegrationEventTypes.LOAN_CONFIRMED.eventName;
	private static final String LOAN_REQUESTED_EVENT_NAME = IntegrationEventTypes.LOAN_REQUESTED.eventName;
	
	private static final String PRODUCER = "loan-service";
	private final LoanIntegrationEventMapper mapper = new LoanIntegrationEventMapper();

	@ParameterizedTest(name = "[{index}] set correct metadata -> {0}")
	@MethodSource("eventCases")
	void shouldSetCorrectMetadata(String name, LoanEvent domainEvent, IntegrationEventTypes eventType) {

		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);

		Assertions.assertEquals(eventType, event.eventType());
		Assertions.assertEquals(PRODUCER, event.producer());
		Assertions.assertEquals(1, event.schemaVersion());
		Assertions.assertNotNull(event.eventId());
	}

	@Test
	void shouldMapLoanIntegrationPayloadCorrectly() {
		
		LoanReservedEvent domainEvent = getLoanReservedEvent();
		
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
		
		LoanIntegrationPayload integrationPayload = (LoanIntegrationPayload) event.payload();
		LoanPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.isbn(), integrationPayload.isbn());
		Assertions.assertEquals(payload.id(), integrationPayload.loanId());
		Assertions.assertEquals(payload.userId(), integrationPayload.userId());
	}
	
	@Test
	void shouldMapLLoanFailedIntegrationPayloadCorrectly() {
		
		LoanFailedEvent domainEvent = getLoanFailedEvent();
		
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
		
		LoanFailedIntegrationPayload integrationPayload = (LoanFailedIntegrationPayload) event.payload();
		LoanFailedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.id(), integrationPayload.loanId());
		Assertions.assertEquals(payload.reason().toString(), integrationPayload.reason());
	}
	
	static Stream<Arguments> eventCases() {
		return Stream.of(Arguments.of(LOAN_REQUESTED_EVENT_NAME, getLoanRequestedEvent(), IntegrationEventTypes.LOAN_REQUESTED),
				Arguments.of(LOAN_CONFIRMED_EVENT_NAME, getLoanConfirmedEvent(), IntegrationEventTypes.LOAN_CONFIRMED),
				Arguments.of(LOAN_CONFIRM_REQUESTED_EVENT_NAME, getLoanConfirmRequestedEvent(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED),
				Arguments.of(LOAN_CANCELED_EVENT_NAME, getLoanCanceledEvent(), IntegrationEventTypes.LOAN_CANCELED),
				Arguments.of(LOAN_RETURNED_EVENT_NAME, getLoanReturnedEvent(), IntegrationEventTypes.LOAN_RETURNED),
				Arguments.of(LOAN_RESERVED_EVENT_NAME, getLoanReservedEvent(), IntegrationEventTypes.LOAN_RESERVED),
				Arguments.of(LOAN_FAILED_EVENT_NAME, getLoanFailedEvent(), IntegrationEventTypes.LOAN_FAILED));
	}

	private static LoanFailedEvent getLoanFailedEvent() {
		LoanFailedPayload payload = new LoanFailedPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0",
				LoanFailedReason.BOOK_NOT_FOUND);
		return new LoanFailedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "d4cc156e-04f8-4fa8-866e-359515bcc104",
				payload, Instant.now());
	}

	private static LoanReservedEvent getLoanReservedEvent() {
		LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
		return new LoanReservedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "f1972050-5803-47e8-9c4c-1f8d46716600",
				payload, Instant.now());
	}

	private static LoanReturnedEvent getLoanReturnedEvent() {
		LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
		return new LoanReturnedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "d282a0bd-0bec-4257-861d-c1585e0a0e93", payload, Instant.now());
	}

	private static LoanCanceledEvent getLoanCanceledEvent() {
		LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
		return new LoanCanceledEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "046f099d-41f9-4c05-8da9-470b789f6a3b", payload, Instant.now());
	}

	private static LoanConfirmRequestedEvent getLoanConfirmRequestedEvent() {
		LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
		return new LoanConfirmRequestedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "355d2c7b-dc0d-4b8a-b27f-acc8375eeaee", payload, Instant.now());
	}

	private static LoanConfirmedEvent getLoanConfirmedEvent() {
		LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
		return new LoanConfirmedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "ebe0e803-afb9-4998-beb0-200773e7c764", payload, Instant.now());
	}

	private static LoanRequestedEvent getLoanRequestedEvent() {
		LoanRequestPayload payload = new LoanRequestPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b", 
				new DateRange(LocalDate.now(), null), LoanStatus.PENDING);
		return new LoanRequestedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "3d209d01-4b1d-4d64-991f-d0a63a7ddecd", payload, Instant.now());
	}
}
