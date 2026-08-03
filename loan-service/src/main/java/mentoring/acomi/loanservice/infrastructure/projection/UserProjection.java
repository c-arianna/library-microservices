package mentoring.acomi.loanservice.infrastructure.projection;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.LibraryCardAssignedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class UserProjection implements EventProjector {

	private final UserViewRepository repository;
		
	private final Map<IntegrationEventTypes, Consumer<IntegrationEventEnvelope<?>>> handlers;
	
	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
		handlers = Map.of(IntegrationEventTypes.USER_SUBSCRIBED, this::handleSubscribeUser,
				IntegrationEventTypes.USER_SUSPENDED, this::handleUpdateUserStatus,
				IntegrationEventTypes.USER_UNSUBSCRIBED, this::handleUpdateUserStatus,
				IntegrationEventTypes.USER_UNSUSPENDED, this::handleUpdateUserStatus,
				IntegrationEventTypes.LIBRARY_CARD_ASSIGNED, this::assignCardNumber);
	}
	@Override
    public boolean supports(IntegrationEventTypes type) {
        return handlers.containsKey(type);
    }

	@Override
	public void project(IntegrationEventEnvelope<?> event) {
		handlers.get(event.eventType()).accept(event);
	}
	
	private void handleSubscribeUser(IntegrationEventEnvelope<?> event) {
		UserSubscribedIntegrationPayload payload = (UserSubscribedIntegrationPayload) event.payload();
		UserView user = new UserView(payload.userId(), payload.email(), payload.name(), payload.lastname(), payload.cardNumber(), 
				payload.userIdentityProviderId(), payload.status());
		repository.add(user, event.occurredAt());
	}

	private void handleUpdateUserStatus(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = (UserIntegrationPayload) event.payload();
		repository.updateStatus(payload.userId(), payload.status(), event.occurredAt());
	}
	
	private void assignCardNumber(IntegrationEventEnvelope<?> event) {
		LibraryCardAssignedIntegrationPayload payload =  (LibraryCardAssignedIntegrationPayload) event.payload();
	    repository.updateCardNumber(payload.userId(), payload.cardNumber(), event.occurredAt());
	}

}
