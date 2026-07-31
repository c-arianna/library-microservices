package mentoring.acomi.loanservice.repository;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;
import mentoring.acomi.loanservice.config.SecurityTestConfig;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class DailyLoanStatisticRepositoryTest {

	@Autowired
    private DailyLoanStatisticRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldInsertFirstCreatedLoanStatistic() {

        LocalDate date = LocalDate.now();

        repository.registerLoanCreated(date);

        entityManager.flush();
        entityManager.clear();

        DailyLoanStatisticView statistic = repository.findByStatisticDate(date).orElseThrow();

        Assertions.assertEquals(1, statistic.loansCreated());
        Assertions.assertEquals(0, statistic.loansConfirmed());
        Assertions.assertEquals(0, statistic.loansCanceled());
        Assertions.assertEquals(0, statistic.loansReturned());
    }
    
    @Test
    void shouldUpdateDifferentCountersForSameDay() {

        LocalDate date = LocalDate.now();

        repository.registerLoanCreated(date);
        repository.registerLoanCreated(date);
        repository.registerLoanConfirmed(date);
        repository.registerLoanReturned(date);
        repository.registerLoanConfirmed(date);
        repository.registerLoanCanceled(date);
        repository.registerLoanReturned(date);
        repository.registerLoanCreated(date);
        repository.registerLoanCanceled(date);
        

        entityManager.flush();
        entityManager.clear();

        DailyLoanStatisticView statistic = repository.findByStatisticDate(date).orElseThrow();

        Assertions.assertEquals(3, statistic.loansCreated());
        Assertions.assertEquals(2, statistic.loansConfirmed());
        Assertions.assertEquals(2, statistic.loansCanceled());
        Assertions.assertEquals(2, statistic.loansReturned());
    }

}
