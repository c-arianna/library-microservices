package mentoring.acomi.userservice.application.migration;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.aggregates.UserAggregateFactory;
import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.model.CardNumber;

@Service
public class CardNumberMigrationService {

    private final UserViewQueryRepository queryRepository;
    private final CardNumberGenerator generator;
    private final UserAggregateFactory aggregateFactory;
    
    public CardNumberMigrationService(UserViewQueryRepository queryRepository, CardNumberGenerator generator, 
    		UserAggregateFactory aggregateFactory) {
        this.queryRepository = queryRepository;
        this.generator = generator;
        this.aggregateFactory = aggregateFactory;
    }

    public void migrate() {

        List<UserView> users = queryRepository.findWithoutCardNumber();

        for (UserView user : users) {
            migrateUser(user);
        }
    }

    @Transactional
	public void migrateUser(UserView user) {
		UserAggregate aggregate = aggregateFactory.create(user.id());
		CardNumber cardNumber = generator.generate();
		aggregate.assignCardNumber(cardNumber.value());
	}
        
}