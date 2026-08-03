package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.projection.LoanProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class LoanProjectionTest {

    @Mock
    private LoanViewRepository repository;

    private LoanProjection projection;

    @BeforeEach
    void setUp() {
        projection = new LoanProjection(repository);
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
    void shouldSupportLoanReservedEvent() {
    	Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_RESERVED));
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
    void shouldSupportLoanFailedEvent() {
    	Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_FAILED));
    }

    @Test
    void shouldNotSupportUserSubscribedEvent() {
    	Assertions.assertFalse(projection.supports(IntegrationEventTypes.USER_SUBSCRIBED));
    }

    @Test
    void shouldInsertLoanWhenLoanRequestedEventArrives() {

        Instant occurredAt = Instant.parse("2026-07-30T10:00:00Z");

        LoanRequestedIntegrationPayload payload = new LoanRequestedIntegrationPayload("loan-1", "9788804336327", "user-1", 
        		LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15));

        IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
                        IntegrationEventTypes.LOAN_REQUESTED, "test", "loan-1", AggregateType.LOAN.name(), 1, occurredAt, 1, payload);

        projection.project(event);

        ArgumentCaptor<LoanView> captor = ArgumentCaptor.forClass(LoanView.class);

        verify(repository).insertRequest(captor.capture(), eq(occurredAt));

        LoanView inserted = captor.getValue();

        Assertions.assertEquals("loan-1", inserted.id());
        Assertions.assertEquals("9788804336327", inserted.isbn());
        Assertions.assertEquals("user-1", inserted.userId());
        Assertions.assertEquals(LoanStatus.PENDING, inserted.status());
        
    }

    @Test
    void shouldConfirmLoan() {

        Instant occurredAt = Instant.now();

        projection.project(loanEvent(IntegrationEventTypes.LOAN_CONFIRMED, new LoanIntegrationPayload("loan-1", "9788804336327", "user-1"), 
        		occurredAt));

        verify(repository).updateStatus("loan-1", LoanStatus.CONFIRMED, occurredAt);
    }

    @Test
    void shouldReserveLoan() {

        Instant occurredAt = Instant.now();

        projection.project(loanEvent(IntegrationEventTypes.LOAN_RESERVED, new LoanIntegrationPayload("loan-1", "9788804336327", "user-1"),
                        occurredAt));

        verify(repository).updateStatus("loan-1", LoanStatus.RESERVED, occurredAt);
    }

    @Test
    void shouldCancelLoan() {

        Instant occurredAt = Instant.now();

        projection.project(loanEvent(IntegrationEventTypes.LOAN_CANCELED, new LoanIntegrationPayload("loan-1", "9788804336327", "user-1"),
                        occurredAt));

        verify(repository).updateStatus("loan-1", LoanStatus.CANCELED, occurredAt);
    }

    @Test
    void shouldFailLoan() {

        Instant occurredAt = Instant.now();

        projection.project(loanEvent(IntegrationEventTypes.LOAN_FAILED, new LoanFailedIntegrationPayload("loan-1", "error"), occurredAt));

        verify(repository).updateStatus("loan-1", LoanStatus.FAILED, occurredAt);
    }

    @Test
    void shouldReturnLoanForSchemaVersionTwo() {

        Instant occurredAt = Instant.parse("2026-07-30T10:00:00Z");

        LocalDate returnedAt = LocalDate.of(2026, 7, 29);

        projection.project(new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_RETURNED,
                        "test", "loan-1", AggregateType.LOAN.name(), 1, occurredAt, 2, new LoanReturnedIntegrationPayload("loan-1",
                        		"9788804336327", "user-1", returnedAt)));

        verify(repository).returnLoan("loan-1", occurredAt, returnedAt);
    }

    @Test
    void shouldUseOccurredAtDateForSchemaVersionOne() {

        Instant occurredAt = Instant.parse("2026-07-30T10:00:00Z");

        projection.project(new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_RETURNED,
                        "test", "loan-1", AggregateType.LOAN.name(), 1, occurredAt, 1, new LoanReturnedIntegrationPayload("loan-1",
                        		"9788804336327", "user-1", null)));

        verify(repository).returnLoan(eq("loan-1"), eq(occurredAt), eq(LocalDate.of(2026, 7, 30)));
    }

    private IntegrationEventEnvelope<?> loanEvent(IntegrationEventTypes type, Object payload, Instant occurredAt) {
        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), type, "test", "loan-1", AggregateType.LOAN.name(), 1,
                occurredAt, 1, payload);
    }
}