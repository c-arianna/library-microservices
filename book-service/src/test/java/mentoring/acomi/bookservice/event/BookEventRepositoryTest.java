package mentoring.acomi.bookservice.event;

import org.junit.jupiter.api.Assertions;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.config.SecurityTestConfig;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.domain.events.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class BookEventRepositoryTest {

	@Autowired
	private BookEventRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Test
	public void shouldAppendEventToStream() {

		String isbn = "9788804336327";
		BookEvent event = createBookRegisteredEvent(isbn);

		repository.appendToStream(event);

		entityManager.clear();

		List<BookEvent> events = repository.loadStream(isbn);

		Assertions.assertEquals(1, events.size());
	}

	@Test
	public void shouldPreserveEventOrder() {

		String isbn = "9788804336327";
		BookRegisteredEvent bookRegisteredEvent = createBookRegisteredEvent(isbn);
		repository.appendToStream(bookRegisteredEvent);

		BookReservedEvent bookReservedEvent = createBookReservedEvent(isbn);
		repository.appendToStream(bookReservedEvent);

		entityManager.clear();

		List<BookEvent> events = repository.loadStream(isbn);

		Assertions.assertEquals(2, events.size());

		Assertions.assertEquals(BookEventType.BookRegistered, events.get(0).type());
		Assertions.assertEquals(BookEventType.BookReserved, events.get(1).type());

	}

	@Test
	public void shouldReturnTrueIfAggregateExists() {

		String isbn = "9788804336327";
		BookRegisteredEvent bookRegisteredEvent = createBookRegisteredEvent(isbn);
		repository.appendToStream(bookRegisteredEvent);

		entityManager.clear();

		boolean exists = repository.exists(isbn, AggregateType.BOOK.name());

		Assertions.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfAggregateDoesNotExist() {

		boolean exists = repository.exists("isbn1", AggregateType.BOOK.name());

		Assertions.assertFalse(exists);
	}

	@Test
	public void shouldReturnEvent() {

		String isbn = "9788804336327";
		BookRegisteredEvent bookRegisteredEvent = createBookRegisteredEvent(isbn);
		repository.appendToStream(bookRegisteredEvent);

		entityManager.clear();
		
		Optional<BookEvent> event = repository.getEvent(BookEventType.BookRegistered.name(), isbn);

		Assertions.assertTrue(event.isPresent());
	}

	@Test
	void shouldReturnEmptyIfEventDoesNotExist() {

		Optional<BookEvent> event = repository.getEvent("EVENT_UNKNOWN", "9788804336327");

		Assertions.assertTrue(event.isEmpty());
	}

	@Test
	void shouldSerializeAndDeserializeEvent() {

		String isbn = "9788804336327";
		BookRegisteredEvent bookRegisteredEvent = createBookRegisteredEvent(isbn);
		repository.appendToStream(bookRegisteredEvent);

		List<BookEvent> loaded = repository.loadStream(isbn);

		Assertions.assertEquals(bookRegisteredEvent.type(), loaded.get(0).type());
	}

	private BookRegisteredEvent createBookRegisteredEvent(String isbn) {
		String eventId = UUID.randomUUID().toString();

		BookRegisteredPayload payload = new BookRegisteredPayload(isbn, "Italo Calvino", "Il barone rampante", "");
		return new BookRegisteredEvent(isbn, eventId, 0, payload, Instant.now());

	}

	private BookReservedEvent createBookReservedEvent(String isbn) {

		String eventId = UUID.randomUUID().toString();

		BookLoanPayload payload = new BookLoanPayload(isbn, "loan-01", "user-01");
		return new BookReservedEvent(isbn, eventId, 0, payload, Instant.now());

	}

}
