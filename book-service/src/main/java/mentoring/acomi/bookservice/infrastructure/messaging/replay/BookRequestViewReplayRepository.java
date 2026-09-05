package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.sharedcorelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayBookRequestRepo")
public class BookRequestViewReplayRepository implements BookRequestViewRepository, ReplayProjection{

	private static final String TABLE_MAIN = "book_request_view";
	private static final String TABLE_TMP = "book_request_view_tmp";

	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
	
	public BookRequestViewReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.replayTableManager = replayTableManager;
	}

	@Override
	public void createTempTable() {
		replayTableManager.createTempTable(TABLE_MAIN, TABLE_TMP);		
	}

	@Override
	public void swapTables() {
		replayTableManager.swapTables(TABLE_MAIN, TABLE_TMP);		
	}

	@Override
	public void dropTempTable() {
		replayTableManager.dropTempTable(TABLE_TMP);		
	}

	@Override
	public void add(BookRequestView view) {
		jdbcTemplate.update("""
				INSERT INTO %s(request_id, requester_user_id, title, author, isbn, notes, status, votes, created_at, updated_at)
				VALUES (?,?,?,?,?,?,?,?,?,?)""".formatted(TABLE_TMP),
				view.requestId(), view.requesterUserId(), view.title(), view.author(), view.isbn(), view.notes(), view.status().name(), view.votes(),
				view.createdAt(), view.updatedAt());
	}

	@Override
	public void registerVotes(String requestId, int votes, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET votes = votes + ?, updated_at = ? WHERE request_id = ?".formatted(TABLE_TMP), 
				votes, updatedAt, requestId);
	}

	@Override
	public void updateStatus(String requestId, BookRequestStatus status, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET status = ?, updated_at = ? WHERE request_id = ?".formatted(TABLE_TMP), 
				status.name(), updatedAt, requestId);
		
	}
	
	@Override
	public void updatePrice(String requestId, BigDecimal estimatedPrice, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET estimated_price = ?, updated_at = ? WHERE request_id = ?".formatted(TABLE_TMP), 
				estimatedPrice, updatedAt, requestId);
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");		
	}

}
