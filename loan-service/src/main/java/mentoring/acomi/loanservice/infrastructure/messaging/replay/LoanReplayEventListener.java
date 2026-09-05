package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.eventstore.replay.ReplayRequestedEvent;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;

@Component
public class LoanReplayEventListener {

private final LoanReplayService replayService;
	
	public LoanReplayEventListener(LoanReplayService replayService) {
		this.replayService = replayService;
	}
	
	@RabbitListener(queues = MessagingTopology.REPLAY_LOAN_QUEUE)
	public void onReplayRequested(ReplayRequestedEvent event) {

		if (!event.targetService().equals("loan-service")) {
			return;
		}

		replayService.rebuild();
	}
}
