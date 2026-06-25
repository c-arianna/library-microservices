package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.AbstractBookProjection;

@Component
public class BookProjectionReplay extends AbstractBookProjection {

	public BookProjectionReplay(@Qualifier("replayRepo") BookViewReplayRepository repository) {
		super(repository);
	}

}