package mentoring.acomi.loanservice.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;
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

		LoanView loan = new LoanView(loanId, "9788804336327", "user01", LocalDate.now(), null, LoanStatus.PENDING, null);
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
		
		Optional<LoanView> loanView = queryRepository.findById(loanId);;
		Assertions.assertTrue(loanView.isPresent());
		Assertions.assertEquals(LoanStatus.CONFIRMED, loanView.get().status());
		
	}
	
	@Test
	public void shouldUpdateReturnedAt() {
		
		String loanId = UUID.randomUUID().toString();
		insertLoan(loanId);
		
		repository.updateStatus(loanId, LoanStatus.CONFIRMED, Instant.now());
		
		LocalDate returnedAt = LocalDate.now();
		repository.returnLoan(loanId, Instant.now(), returnedAt);
		
		entityManager.clear();
		
		Optional<LoanView> loanView = queryRepository.findById(loanId);;
		Assertions.assertTrue(loanView.isPresent());
		Assertions.assertEquals(LoanStatus.RETURNED, loanView.get().status());
		Assertions.assertEquals(returnedAt, loanView.get().returnedAt());
		
	}

	@Test
	public void shouldReturnsLoansOverdue() {
		
		String loanId = UUID.randomUUID().toString();
		insertLoan(loanId);
		
		repository.updateStatus(loanId, LoanStatus.CONFIRMED, Instant.now());
		
		LocalDate returnedAt = LocalDate.now();
		repository.returnLoan(loanId, Instant.now(), returnedAt);
		
		loanId = UUID.randomUUID().toString();
		LocalDate startDate = LocalDate.now().minusDays(10);
		LocalDate endDate = startDate.plusDays(2);
		
		insertLoan(loanId,startDate, endDate);
		
		repository.updateStatus(loanId, LoanStatus.CONFIRMED, Instant.now());
		
		entityManager.clear();
		
		List<LoanDto> loansOverdue = queryRepository.getLoansOverdue();
		
		Assertions.assertEquals(1, loansOverdue.size());
		
		LoanDto loan = loansOverdue.get(0);
		
		Assertions.assertEquals(loanId, loan.id());
		Assertions.assertEquals(endDate, loan.endDate());
		Assertions.assertEquals(LoanStatus.CONFIRMED, loan.status());
		
	}
	
	private void insertLoan(String loanId) {
		LocalDate startDate = LocalDate.now();
		insertLoan(loanId, startDate, startDate.plusDays(30));
	}
	
	private void insertLoan(String loanId, LocalDate startDate, LocalDate endDate) {
		LoanViewEntity entity = new LoanViewEntity(loanId, "9788804336327", "user01", startDate, endDate);
		entity.markCreated(Instant.now());
		jpaRepository.saveAndFlush(entity);
	}

}