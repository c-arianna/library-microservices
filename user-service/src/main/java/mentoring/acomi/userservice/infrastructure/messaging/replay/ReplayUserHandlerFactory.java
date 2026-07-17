package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.userservice.application.projection.UserProjectionOperations;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserSubscribedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserSuspendedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserUnsubscribedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserUnsuspendedV1Handler;

@Component
public class ReplayUserHandlerFactory {

    private final UserProjectionOperations replayProjection;
	private final EventPayloadMapper mapper;

	public ReplayUserHandlerFactory(@Qualifier("replayUserProjection") UserProjectionOperations userReplayProjection, EventPayloadMapper mapper) {
		this.replayProjection = userReplayProjection;
		this.mapper = mapper;
	}

	public List<EventHandler> createHandlers() {

		return List.of(new UserSubscribedV1Handler(replayProjection, mapper),
				       new UserSuspendedV1Handler(replayProjection, mapper),
				       new UserUnsubscribedV1Handler(replayProjection, mapper),
				       new UserUnsuspendedV1Handler(replayProjection, mapper)     
		);
	}
}