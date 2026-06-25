package mentoring.acomi.userservice.infrastructure.messaging.replay;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedlibrary.eventstore.replay.ReplayRequestedEvent;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Component
public class UserReplayEventListener {

	private final UserReplayService replayService;
	
	public UserReplayEventListener(UserReplayService replayService) {
		this.replayService = replayService;
	}
	
	@RabbitListener(queues = MessagingTopology.REPLAY_USER_QUEUE)
	public void onReplayRequested(ReplayRequestedEvent event) {

		if (!event.targetService().equals("user-service")) {
			return;
		}

		replayService.rebuild();
	}
}
