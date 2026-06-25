package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedlibrary.eventstore.replay.ReplayRequestedEvent;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Component
public class BookReplayEventListener {

	private final BookReplayService replayService;
	
	public BookReplayEventListener(BookReplayService replayService) {
		this.replayService = replayService;
	}
	
	@RabbitListener(queues = MessagingTopology.REPLAY_BOOK_QUEUE)
	public void onReplayRequested(ReplayRequestedEvent event) {

		if (!event.targetService().equals("book-service")) {
			return;
		}

		replayService.rebuild();
	}

}
