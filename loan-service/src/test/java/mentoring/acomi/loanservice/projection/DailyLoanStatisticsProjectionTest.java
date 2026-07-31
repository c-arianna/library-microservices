package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.projection.DailyLoanStatisticProjection;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;

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
    void shouldRegisterLoanCreated() {

        LocalDate today = LocalDate.now();

        projection.registerLoanCreated(today);

        verify(repository).registerLoanCreated(today);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldRegisterLoanConfirmed() {

        LocalDate today = LocalDate.now();

        projection.registerLoanConfirmed(today);

        verify(repository).registerLoanConfirmed(today);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldRegisterLoanCanceled() {

        LocalDate today = LocalDate.now();

        projection.registerLoanCanceled(today);

        verify(repository).registerLoanCanceled(today);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldRegisterLoanReturned() {

        LocalDate today = LocalDate.now();

        projection.registerLoanReturned(today);

        verify(repository).registerLoanReturned(today);
        verifyNoMoreInteractions(repository);
    }

}
