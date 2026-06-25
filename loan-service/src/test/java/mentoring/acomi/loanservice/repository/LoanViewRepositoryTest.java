package mentoring.acomi.loanservice.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.config.SecurityTestConfig;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanViewJpaRepository;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class LoanViewRepositoryTest {

	@Autowired
	private LoanViewQueryRepository queryRepository;
	
	@Autowired
	private LoanViewRepository repository;

	@Autowired
	private LoanViewJpaRepository jpaRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	public void shouldSaveLoanView() {

		String loanId = UUID.randomUUID().toString();
		Optional<LoanView> loanView = queryRepository.findById(loanId);
		Assertions.assertTrue(loanView.isEmpty());

		LoanView loan = new LoanView(loanId, "9788804336327", "user01", LocalDate.now(), null, LoanStatus.PENDING);
		repository.insertRequest(loan, Instant.now());
		loanView = queryRepository.findById(loanId);
		
		Assertions.assertTrue(loanView.isPresent());

		LoanView loanViewFound = loanView.get();

		Assertions.assertAll(() -> Assertions.assertEquals("9788804336327", loanViewFound.isbn()),
				() -> Assertions.assertEquals("user01", loanViewFound.userId()),
				() -> Assertions.assertEquals(LoanStatus.PENDING, loanViewFound.status())
		);

	}
	
	@Test
	public void shouldUpdateStatus() {
		String loanId = UUID.randomUUID().toString();
		insertLoan(loanId);
			
		repository.updateStatus(loanId, LoanStatus.CONFIRMED, Instant.now());
		
		entityManager.clear();
		
		LoanView loanView = queryRepository.findById(loanId).orElseThrow();;
		
		Assertions.assertEquals(LoanStatus.CONFIRMED, loanView.status());
		
	}

	private void insertLoan(String loanId) {
		LoanViewEntity entity = new LoanViewEntity(loanId, "9788804336327", "user01", LocalDate.now(), null);
		entity.markCreated(Instant.now());
		jpaRepository.saveAndFlush(entity);
	}

}