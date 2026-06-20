package mentoring.acomi.loanservice.event;

import org.junit.jupiter.api.Assertions;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.config.SecurityTestConfig;
import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class LoanEventRepositoryTest {

	@Autowired
	private LoanEventRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Test
	public void shouldAppendEventToStream() {

		String loanId = UUID.randomUUID().toString();
		LoanRequestedEvent event = createLoanRequestedEvent(loanId, "9788804336327");

		repository.appendToStream(event);

		entityManager.clear();

		List<LoanEvent> events = repository.loadStream(loanId);

		Assertions.assertEquals(1, events.size());
	}

	@Test
	public void shouldPreserveEventOrder() {

		String isbn = "9788804336327";
		String loanId = UUID.randomUUID().toString();
		LoanRequestedEvent loanRequestedEvent = createLoanRequestedEvent(loanId, isbn);
		repository.appendToStream(loanRequestedEvent);

		LoanCanceledEvent loanCanceledEvent = createLoanCanceledEvent(loanId, isbn);
		repository.appendToStream(loanCanceledEvent);

		entityManager.clear();

		List<LoanEvent> events = repository.loadStream(loanId);

		Assertions.assertEquals(2, events.size());

		Assertions.assertEquals(LoanEventType.LoanRequested, events.get(0).type());
		Assertions.assertEquals(LoanEventType.LoanCanceled, events.get(1).type());

	}

	@Test
	public void shouldReturnTrueIfAggregateExists() {

		String isbn = "9788804336327";
		String loanId = UUID.randomUUID().toString();
		LoanRequestedEvent loanRequestedEvent = createLoanRequestedEvent(loanId, isbn);
		repository.appendToStream(loanRequestedEvent);

		entityManager.clear();

		boolean exists = repository.exists(loanId);

		Assertions.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfAggregateDoesNotExist() {

		boolean exists = repository.exists("loan01");

		Assertions.assertFalse(exists);
	}

	@Test
	public void shouldReturnEvent() {

		String isbn = "9788804336327";
		String loanId = UUID.randomUUID().toString();
		LoanRequestedEvent loanRequestedEvent = createLoanRequestedEvent(loanId, isbn);
		repository.appendToStream(loanRequestedEvent);

		entityManager.clear();
		
		Optional<LoanEvent> event = repository.getEvent(LoanEventType.LoanRequested.name(), loanId);

		Assertions.assertTrue(event.isPresent());
	}

	@Test
	void shouldReturnEmptyIfEventDoesNotExist() {

		Optional<LoanEvent> event = repository.getEvent("EVENT_UNKNOWN", "loan01");

		Assertions.assertTrue(event.isEmpty());
	}

	@Test
	void shouldSerializeAndDeserializeEvent() {

		String isbn = "9788804336327";
		String loanId = UUID.randomUUID().toString();
		LoanRequestedEvent loanRequestedEvent = createLoanRequestedEvent(loanId, isbn);
		repository.appendToStream(loanRequestedEvent);

		List<LoanEvent> loaded = repository.loadStream(loanId);

		Assertions.assertEquals(loanRequestedEvent.type(), loaded.get(0).type());
	}

	private LoanRequestedEvent createLoanRequestedEvent(String loanId, String isbn) {
		String eventId = UUID.randomUUID().toString();

		LoanRequestPayload payload = new LoanRequestPayload(loanId, isbn, "user01", new DateRange(LocalDate.now(), null), LoanStatus.PENDING);
		return new LoanRequestedEvent(loanId, eventId, 0, payload, Instant.now());

	}

	private LoanCanceledEvent createLoanCanceledEvent(String loanId, String isbn) {

		String eventId = UUID.randomUUID().toString();

		LoanPayload payload = new LoanPayload(loanId, isbn, "user-01");
		return new LoanCanceledEvent(loanId, eventId, 0, payload, Instant.now());

	}

}
