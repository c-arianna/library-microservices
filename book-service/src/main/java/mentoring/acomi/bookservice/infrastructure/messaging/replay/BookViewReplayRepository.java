package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.sharedlibrary.eventstore.replay.BaseReplayRepository;
import mentoring.acomi.sharedlibrary.replay.TempTableCreator;

@Repository("replayRepo")
public class BookViewReplayRepository extends BaseReplayRepository implements BookViewRepository {

	private static final String TABLE_MAIN = "book_view";
	private static final String TABLE_TMP = "book_view_tmp";

	private final TempTableCreator tableCreator;
	
	public BookViewReplayRepository(JdbcTemplate jdbcTemplate, TempTableCreator tableCreator) {
		super(jdbcTemplate, TABLE_MAIN, TABLE_TMP);
		this.tableCreator = tableCreator;
	}

	public void createTempTable() {
		tableCreator.createTempTable(TABLE_TMP, TABLE_MAIN);
	}

	@Override
	public void addBook(BookView book, Instant createdAt) {
		jdbcTemplate.update("INSERT INTO %s(isbn, author, title, description, created_at, updated_at) VALUES (?,?,?,?,?,?)".formatted(TABLE_TMP),
				book.isbn(), book.author(), book.title(), book.description(), createdAt, createdAt);
	}

	@Override
	public void updateCopies(String isbn, int quantity, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET total_copies = total_copies + ?, updated_at = ? WHERE isbn = ?".formatted(TABLE_TMP), quantity, updatedAt, isbn);
	}

	@Override
	public void reserve(String isbn, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET reserved_copies = reserved_copies + 1, updated_at = ? WHERE isbn = ?".formatted(TABLE_TMP), updatedAt, isbn);
	}

	@Override
	public void borrow(String isbn, Instant updatedAt) {
		jdbcTemplate.update("""
				 UPDATE %s SET borrowed_copies = borrowed_copies + 1, reserved_copies =
					    CASE
					        WHEN reserved_copies > 0
					        	THEN reserved_copies - 1
					        	ELSE reserved_copies
					     	END,
				    	updated_at = ?
						WHERE isbn = ?
				""".formatted(TABLE_TMP), updatedAt, isbn);
	}

	@Override
	public void release(String isbn, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET reserved_copies = reserved_copies -1,  updated_at = ? WHERE isbn = ? AND reserved_copies >0".formatted(TABLE_TMP), 
				updatedAt, isbn);
	}

	@Override
	public void returnBorrowed(String isbn, Instant updatedAt) {
		jdbcTemplate.update(
				"UPDATE %s SET borrowed_copies = borrowed_copies -1,  updated_at = ? WHERE isbn = ? AND borrowed_copies >0".formatted(TABLE_TMP),
				updatedAt, isbn);
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");
	}

}
