package mentoring.acomi.bookservice.repository;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.config.SecurityTestConfig;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookViewJpaRepository;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class BookViewRepositoryTest {

	@Autowired
	private BookViewRepository repository;
	
	@Autowired
	private BookViewQueryRepository queryRepository;

	@Autowired
	private BookViewJpaRepository jpaRepository;

	@Autowired
	private EntityManager entityManager;

	private static final String BOOK_ISBN = "9788804336327";
	private static final String BOOK_AUTHOR = "Italo Calvino";
	private static final String BOOK_TITLE = "Il barone rampante";
	private static final String BOOK_DESCRIPTION = "";
	private static final int BOOK_TOTAL_COPIES = 0;
	private static final int BOOK_AVAILABLE_COPIES = 0;
	private static final int BOOK_BORROWED_COPIES = 0;
	private static final int BOOK_RESERVED_COPIES = 0;
	private static final int QUANTITY = 3;

	@Test
	void shouldSaveBookView() {

		Optional<BookView> bookView = queryRepository.findById(BOOK_ISBN);
		Assertions.assertTrue(bookView.isEmpty());

		BookView book = new BookView(BOOK_ISBN, BOOK_AUTHOR, BOOK_TITLE, BOOK_DESCRIPTION, BOOK_TOTAL_COPIES,
				BOOK_AVAILABLE_COPIES, BOOK_BORROWED_COPIES, BOOK_RESERVED_COPIES);
		repository.addBook(book, Instant.now());
		bookView = queryRepository.findById(BOOK_ISBN);

		Assertions.assertTrue(bookView.isPresent());

		BookView bookViewFound = bookView.get();

		Assertions.assertAll(() -> Assertions.assertEquals(BOOK_AUTHOR, bookViewFound.author()),
				() -> Assertions.assertEquals(BOOK_TITLE, bookViewFound.title()),
				() -> Assertions.assertEquals(BOOK_DESCRIPTION, bookViewFound.description()),
				() -> Assertions.assertEquals(BOOK_AVAILABLE_COPIES, bookViewFound.availableCopies()),
				() -> Assertions.assertEquals(BOOK_BORROWED_COPIES, bookViewFound.borrowedCopies()),
				() -> Assertions.assertEquals(BOOK_RESERVED_COPIES, bookViewFound.reservedCopies()),
				() -> Assertions.assertEquals(BOOK_TOTAL_COPIES, bookViewFound.totalCopies()));

	}

	@Test
	void reserveShouldIncrementReservedCopies() {

		insertBookWithCopies(BOOK_ISBN, 5, 0, 0);

		repository.reserve(BOOK_ISBN, Instant.now());

		entityManager.clear();
		
		BookView found = queryRepository.findById(BOOK_ISBN).orElseThrow();
		Assertions.assertEquals(1, found.reservedCopies());
		Assertions.assertEquals(4, found.availableCopies());

	}

	@Test
	void borrowShouldIncrementBorrowedCopies() {

		insertBookWithCopies(BOOK_ISBN, 5, 0, 1);

		repository.borrow(BOOK_ISBN, Instant.now());

		entityManager.clear();
		
		BookView found = queryRepository.findById(BOOK_ISBN).orElseThrow();
		Assertions.assertEquals(1, found.borrowedCopies());
		Assertions.assertEquals(0, found.reservedCopies());
		Assertions.assertEquals(4, found.availableCopies());

	}

	@Test
	void releaseShouldDecrementReservedCopies() {

		insertBookWithCopies(BOOK_ISBN, 5, 0, 1);

		repository.release(BOOK_ISBN, Instant.now());

		entityManager.clear();
		
		BookView found = queryRepository.findById(BOOK_ISBN).orElseThrow();
		Assertions.assertEquals(0, found.reservedCopies());
		Assertions.assertEquals(5, found.availableCopies());

	}

	@Test
	void returnShouldDecrementBorrowedCopies() {

		insertBookWithCopies(BOOK_ISBN, 5, 1, 1);

		repository.returnBorrowed(BOOK_ISBN, Instant.now());

		entityManager.clear();
		
		BookView found = queryRepository.findById(BOOK_ISBN).orElseThrow();
		Assertions.assertEquals(0, found.borrowedCopies());
		Assertions.assertEquals(4, found.availableCopies());

	}

	@Test
	void addCopiesShouldIncrementTotalCopies() {

		insertBookWithCopies(BOOK_ISBN, 5, 1, 1);

		repository.updateCopies(BOOK_ISBN, QUANTITY, Instant.now());

		entityManager.clear();
		
		BookView found = queryRepository.findById(BOOK_ISBN).orElseThrow();
		Assertions.assertEquals(8, found.totalCopies());
		Assertions.assertEquals(6, found.availableCopies());

	}

	@Test
	void removeCopiesShouldDecrementTotalCopies() {

		insertBookWithCopies(BOOK_ISBN, 5, 1, 1);

		repository.updateCopies(BOOK_ISBN, -QUANTITY, Instant.now());

		entityManager.clear();
		
		BookView found = queryRepository.findById(BOOK_ISBN).orElseThrow();
		Assertions.assertEquals(2, found.totalCopies());
		Assertions.assertEquals(0, found.availableCopies());

	}

	private void insertBookWithCopies(String isbn, int totalCopies, int borrowedCopies, int reservedCopied) {
		BookViewEntity entity = new BookViewEntity(isbn, BOOK_AUTHOR, BOOK_TITLE, BOOK_DESCRIPTION, totalCopies,
				borrowedCopies, reservedCopied);
		entity.markCreated(Instant.now());
		jpaRepository.saveAndFlush(entity);
	}

}