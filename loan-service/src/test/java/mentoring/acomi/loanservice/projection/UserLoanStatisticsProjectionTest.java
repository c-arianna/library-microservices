package mentoring.acomi.loanservice.projection;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.projection.UserLoanStatisticProjection;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

@ExtendWith(MockitoExtension.class)
public class UserLoanStatisticsProjectionTest {

    private static final String USER_ID = "USER-1";

	private static final String LOAN_ID = "LOAN-1";

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
    void shouldInsertStatisticsForFirstOverdueLoan() {

    	LoanView loan = createLoan();
    	
        when(statisticRepository.getUserLoanStatistic(USER_ID)).thenReturn(Optional.empty());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        
        projection.registerOverdueLoan(USER_ID, LOAN_ID, LocalDate.of(2026, 7, 5));

        verify(statisticRepository).insert(
                argThat(stat ->
                        stat.userId().equals(USER_ID)
                        && stat.overdueLoansCount() == 1
                        && stat.totalDaysOverdue() == 4L
                        && stat.lastOverdueDate().equals(LocalDate.of(2026, 7, 5))
                ));
    }

    @Test
    void shouldUpdateExistingStatistics() {

        UserLoanStatisticView existing = new UserLoanStatisticView(USER_ID, 2, 10, LocalDate.of(2026, 7, 3));
        LoanView loan = createLoan();
        
        when(statisticRepository.getUserLoanStatistic(USER_ID)).thenReturn(Optional.of(existing));
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        
        projection.registerOverdueLoan(USER_ID, LOAN_ID, LocalDate.of(2026, 7, 5));

        verify(statisticRepository).statisticUpdate(eq(USER_ID), eq(4L), eq(LocalDate.of(2026, 7, 5)));
    }

    @Test
    void shouldIgnoreLoanReturnedOnTime() {

    	LoanView loan = createLoan();
    	when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        
    	projection.registerOverdueLoan(USER_ID, LOAN_ID, LocalDate.of(2026, 6, 30));

        verifyNoInteractions(statisticRepository);
    }
    
    private LoanView createLoan() {
		LocalDate startDate = LocalDate.of(2026, 6, 1);
    	LocalDate endDate = startDate.plusDays(30);
    	return new LoanView(LOAN_ID, "9788804336327", USER_ID, startDate, endDate, LoanStatus.CONFIRMED, null);
		
	}
}