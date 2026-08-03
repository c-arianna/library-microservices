package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.projection.DailyLoanStatisticProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class DailyLoanStatisticsProjectionTest {

	@Mock
	private DailyLoanStatisticRepository repository;

	private DailyLoanStatisticProjection projection;

	@BeforeEach
	void setUp() {
		projection = new DailyLoanStatisticProjection(repository);
	}

	@Test
	void shouldSupportLoanRequestedEvent() {
		Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_REQUESTED));
	}

	@Test
	void shouldSupportLoanConfirmedEvent() {
		Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_CONFIRMED));
	}

	@Test
	void shouldSupportLoanCanceledEvent() {
		Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_CANCELED));
	}

	@Test
	void shouldSupportLoanReturnedEvent() {
		Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_RETURNED));
	}

	@Test
	void shouldNotSupportUserEvents() {
		Assertions.assertFalse(projection.supports(IntegrationEventTypes.USER_SUBSCRIBED));
	}

	@Test
	void shouldRegisterLoanCreated() {

		IntegrationEventEnvelope<?> event = event(IntegrationEventTypes.LOAN_REQUESTED);

		projection.project(event);

		verify(repository).registerLoanCreated(expectedDate(event));

		verifyNoMoreInteractions(repository);
	}

	@Test
	void shouldRegisterLoanConfirmed() {

		IntegrationEventEnvelope<?> event = event(IntegrationEventTypes.LOAN_CONFIRMED);

		projection.project(event);

		verify(repository).registerLoanConfirmed(expectedDate(event));

		verifyNoMoreInteractions(repository);
	}

	@Test
	void shouldRegisterLoanCanceled() {

		IntegrationEventEnvelope<?> event = event(IntegrationEventTypes.LOAN_CANCELED);

		projection.project(event);

		verify(repository).registerLoanCanceled(expectedDate(event));

		verifyNoMoreInteractions(repository);
	}

	@Test
	void shouldRegisterLoanReturned() {

		IntegrationEventEnvelope<?> event = event(IntegrationEventTypes.LOAN_RETURNED);

		projection.project(event);

		verify(repository).registerLoanReturned(expectedDate(event));

		verifyNoMoreInteractions(repository);
	}

	private IntegrationEventEnvelope<?> event(IntegrationEventTypes eventType) {

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), eventType, "projection-test", UUID.randomUUID().toString(), 
				AggregateType.LOAN.name(), 1, Instant.now(), 1, null);
	}

	private LocalDate expectedDate(IntegrationEventEnvelope<?> event) {

		return event.occurredAt().atZone(ZoneId.of("Europe/Rome")).toLocalDate();
	}
}