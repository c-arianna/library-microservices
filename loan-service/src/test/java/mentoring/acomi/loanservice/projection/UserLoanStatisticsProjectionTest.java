package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any; 
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.projection.UserLoanStatisticProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class UserLoanStatisticsProjectionTest {

    @Mock
    private UserLoanStatisticRepository statisticRepository;

    @Mock
    private LoanViewQueryRepository loanRepository;

    private UserLoanStatisticProjection projection;

    @BeforeEach
    void setUp() {
        projection = new UserLoanStatisticProjection(statisticRepository, loanRepository);
    }

    @Test
    void shouldSupportLoanReturnedEvent() {
        Assertions.assertTrue(projection.supports(IntegrationEventTypes.LOAN_RETURNED));
    }

    @Test
    void shouldNotSupportLoanConfirmedEvent() {
    	 Assertions.assertFalse(projection.supports(IntegrationEventTypes.LOAN_CONFIRMED));
    }

    @Test
    void shouldIgnoreLoanReturnedWithoutDelay() {

        String userId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();

        LocalDate startDate = LocalDate.now();
		LoanView loan = new LoanView(loanId, "9788804336327", userId, startDate, startDate.plusDays(10), LoanStatus.CONFIRMED, null);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        LocalDate returnedAt = loan.end();

        projection.project(loanReturnedEvent(userId, loanId, returnedAt, 2));

        verifyNoInteractions(statisticRepository);
    }

    @Test
    void shouldInsertStatisticForFirstOverdueLoan() {

        String userId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();

        LocalDate dueDate = LocalDate.of(2026, 7, 20);
        LocalDate returnedAt = LocalDate.of(2026, 7, 25);

        LoanView loan = new LoanView(loanId, "9788804336327", userId, dueDate.minusDays(14), dueDate, LoanStatus.CONFIRMED, null);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        when(statisticRepository.getUserLoanStatistic(userId)).thenReturn(Optional.empty());

        projection.project(loanReturnedEvent(userId, loanId, returnedAt, 2));

        ArgumentCaptor<UserLoanStatisticView> captor = ArgumentCaptor.forClass(UserLoanStatisticView.class);

        verify(statisticRepository).insert(captor.capture());

        UserLoanStatisticView inserted = captor.getValue();

        Assertions.assertEquals(userId, inserted.userId());
        Assertions.assertEquals(1, inserted.overdueLoansCount());
        Assertions.assertEquals(5, inserted.totalDaysOverdue());
        Assertions.assertEquals(returnedAt, inserted.lastOverdueDate());

        verify(statisticRepository, never()).statisticUpdate(anyString(), anyLong(), any());
    }

    @Test
    void shouldUpdateExistingUserStatistic() {

        String userId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();

        LocalDate dueDate = LocalDate.of(2026, 7, 20);
        LocalDate returnedAt = LocalDate.of(2026, 7, 23);

        LoanView loan = new LoanView(loanId, "9788804336327", userId, dueDate.minusDays(14), dueDate, LoanStatus.CONFIRMED, null);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        when(statisticRepository.getUserLoanStatistic(userId)).thenReturn(Optional.of(new UserLoanStatisticView(userId, 1, 10, dueDate)));

        projection.project(loanReturnedEvent(userId, loanId, returnedAt, 2));

        verify(statisticRepository).statisticUpdate(userId, 3, returnedAt);

        verify(statisticRepository, never()).insert(any());
    }

    @Test
    void shouldUseOccurredAtForSchemaVersionOne() {

        String userId = UUID.randomUUID().toString();
        String loanId = UUID.randomUUID().toString();

        Instant occurredAt = Instant.parse("2026-07-25T10:00:00Z");

        LocalDate dueDate = LocalDate.of(2026, 7, 20);

        LoanView loan = new LoanView(loanId, "9788804336327", userId, dueDate.minusDays(14), dueDate, LoanStatus.CONFIRMED, null);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        when(statisticRepository.getUserLoanStatistic(userId)).thenReturn(Optional.empty());

        projection.project(loanReturnedEventV1(userId, loanId, occurredAt));

        verify(statisticRepository).insert(any(UserLoanStatisticView.class));
    }

    private IntegrationEventEnvelope<LoanReturnedIntegrationPayload> loanReturnedEvent(String userId, String loanId, LocalDate returnedAt,
            int schemaVersion) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_RETURNED, "test", loanId,
                AggregateType.LOAN.name(), 1, Instant.now(), schemaVersion, new LoanReturnedIntegrationPayload(loanId, "9788804336327", 
                		userId, returnedAt));
    }

    private IntegrationEventEnvelope<LoanReturnedIntegrationPayload> loanReturnedEventV1(String userId, String loanId, Instant occurredAt) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_RETURNED, "test", loanId,
                AggregateType.LOAN.name(), 1, occurredAt, 1, new LoanReturnedIntegrationPayload(loanId, "9788804336327", userId, null));
    }
}