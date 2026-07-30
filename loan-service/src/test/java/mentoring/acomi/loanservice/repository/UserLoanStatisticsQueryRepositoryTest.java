package mentoring.acomi.loanservice.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.loanservice.config.SecurityTestConfig;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class UserLoanStatisticsQueryRepositoryTest {

	@Autowired
    private UserLoanStatisticQueryRepository queryRepository;
	
	@Autowired
	private UserLoanStatisticRepository repository;
	
	@Autowired
	private LoanViewRepository loanRepository;
		
	@Autowired
	private EntityManager entityManager;
	
    @Test
    void shouldCountActiveOverdueLoans() {

    	UserLoanStatisticView view = new UserLoanStatisticView("USER-1", 3, 8, LocalDate.of(2026, 7, 10));
    	repository.insert(view);
    	
    	LoanView loan = new LoanView("L1", "9788804336327", "USER-1", LocalDate.of(2026, 7, 1), LocalDate.now().minusDays(5), LoanStatus.PENDING, null);
    	loanRepository.insertRequest(loan, Instant.now());
    	
    	loanRepository.updateStatus("L1", LoanStatus.CONFIRMED, Instant.now());
    	
    	entityManager.flush();
    	
        List<OverdueStatisticDto> statistics = queryRepository.getOverdueStatistics(LocalDate.now());

        Assertions.assertEquals(1, statistics.size());

        OverdueStatisticDto dto = statistics.getFirst();

        Assertions.assertEquals(3, dto.overdueLoansCount());
        Assertions.assertEquals(1, dto.activeOverdueLoansCount());
    }
}
