package mentoring.acomi.bookservice.application.repositories;

import java.util.List;

import mentoring.acomi.bookservice.application.dto.BookRequestVoteDto;

public interface BookRequestVoteViewQueryRepository {
	List<BookRequestVoteDto> findVotesByRequestId(String requestId);
}
