package mentoring.acomi.userservice.application.migration;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.domain.model.CardNumber;
import mentoring.acomi.userservice.infrastructure.messaging.UserIntegrationPublisherEventVersions;

@Service
public class CardNumberMigrationService {

    private final UserViewQueryRepository queryRepository;
    private final UserEventRepository userEventRepository;
    private final CardNumberGenerator generator;
    private final EventDispatcher eventDispatcher;

    private final Logger logger = LogManager.getLogger(CardNumberMigrationService.class);
    
    public CardNumberMigrationService(UserViewQueryRepository queryRepository, UserEventRepository userEventRepository, 
    		CardNumberGenerator generator, EventDispatcher eventDispatcher) {
        this.queryRepository = queryRepository;
        this.userEventRepository = userEventRepository;
        this.generator = generator;
        this.eventDispatcher = eventDispatcher;
    }

    public void migrate() {

        List<UserView> users = queryRepository.findWithoutCardNumber();

        for (UserView user : users) {
            UserAggregate aggregate = loadUser(user.id());
            CardNumber cardNumber = generator.generate();
			aggregate.assignCardNumber(cardNumber.value());
        }
    }
    
    private UserAggregate loadUser(String userId) {
		List<UserEvent> events = userEventRepository.loadStream(userId);
		Consumer<UserEvent> dispatch = event -> {
			userEventRepository.appendToStream(event, getSchemaVersion(event.type()));
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new UserAggregate(userId, dispatch, events);
	}
    
    private int getSchemaVersion(UserEventType eventType) {
		
		return switch(eventType) {
			case UserSubscribed -> {
				yield UserIntegrationPublisherEventVersions.USER_SUBSCRIBED;
			}
			case UserUnsubscribed ->{
				yield UserIntegrationPublisherEventVersions.USER_UNSUBSCRIBED;
			}
			case UserSuspended ->{
				yield UserIntegrationPublisherEventVersions.USER_SUSPENDED;
			}
			case UserUnsuspended ->{
				yield UserIntegrationPublisherEventVersions.USER_UNSUSPENDED;
			}
			case LibraryCardAssigned -> {
				yield UserIntegrationPublisherEventVersions.LIBRARY_CARD_ASSIGNED;
			}
		};
	}
}