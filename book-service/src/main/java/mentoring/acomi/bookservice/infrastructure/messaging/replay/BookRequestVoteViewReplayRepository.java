package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestVoteView;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayBookRequestVoteRepo")
public class BookRequestVoteViewReplayRepository implements BookRequestVoteViewRepository, ReplayProjection {

	private static final String TABLE_MAIN = "book_request_vote_view";
	private static final String TABLE_TMP = "book_request_vote_view_tmp";

	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
	
	public BookRequestVoteViewReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
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
	public void add(BookRequestVoteView view) {
		jdbcTemplate.update("""
				INSERT INTO %s(request_id, user_id, created_at) VALUES (?,?,?)""".formatted(TABLE_TMP), 
				view.requestId(), view.userId(), view.createdAt());
		
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");		
	}

}
