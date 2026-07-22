package mentoring.acomi.userservice.infrastructure.messaging.replay;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.LibraryCardAssignedIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.domain.events.payload.LibraryCardAssignedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserPayload;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class UserReplayEventMapper {

    private final ObjectMapper mapper;

	public UserReplayEventMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	public IntegrationEventTypes toIntegrationEventType(UserEventType eventType) {
		
		return switch (eventType) {
		case UserSubscribed -> {
			yield IntegrationEventTypes.USER_SUBSCRIBED;
		}
		case UserSuspended -> {
		    yield IntegrationEventTypes.USER_SUSPENDED;
		}
		case UserUnsubscribed -> {
		    yield IntegrationEventTypes.USER_UNSUBSCRIBED;
		}
		case UserUnsuspended-> {
		    yield IntegrationEventTypes.USER_UNSUSPENDED;
		}
		case LibraryCardAssigned -> {
			 yield IntegrationEventTypes.LIBRARY_CARD_ASSIGNED;
		}
		
		};
	}
	
	public Object toIntegrationPayload(UserEventType eventType, JsonNode eventPayload) {
		return switch (eventType) {
		
			case UserSubscribed -> {
				UserSubscribedPayload payload = mapper.convertValue(eventPayload, UserSubscribedPayload.class);
			    yield new UserSubscribedIntegrationPayload(payload.id(), payload.email(), payload.name(), payload.lastname(), 
			    		payload.userIdentityProviderId(), payload.cardNumber(), payload.status(), payload.role());
			}
			case UserSuspended -> {
				UserPayload payload = mapper.convertValue(eventPayload, UserPayload.class);
				yield new UserIntegrationPayload(payload.userId(), UserStatus.SUSPENDED);
			}
			case UserUnsubscribed -> {
				UserUnsubscribedPayload payload = mapper.convertValue(eventPayload, UserUnsubscribedPayload.class);
				yield new UserIntegrationPayload(payload.userId(), UserStatus.DISABLED);
			}
			case UserUnsuspended -> {
				UserPayload payload = mapper.convertValue(eventPayload, UserPayload.class);
				yield new UserIntegrationPayload(payload.userId(), UserStatus.ACTIVE);
			}
			case LibraryCardAssigned -> {
				LibraryCardAssignedPayload payload = mapper.convertValue(eventPayload, LibraryCardAssignedPayload.class);
				yield new LibraryCardAssignedIntegrationPayload(payload.userId(), payload.cardNumber());
			}
		 
		};
	}

}
