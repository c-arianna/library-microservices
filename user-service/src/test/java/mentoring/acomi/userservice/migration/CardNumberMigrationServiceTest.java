package mentoring.acomi.userservice.migration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.aggregates.UserAggregateFactory;
import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.application.migration.CardNumberMigrationService;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.model.CardNumber;

@ExtendWith(MockitoExtension.class)
public class CardNumberMigrationServiceTest {

    private static final String LASTNAME = "Potter";

	private static final String NAME = "Harry";

	private static final String MAIL = "test@gmail.com";

	private static final String CARD_NUMBER = "LIB-000001";

	@Mock
    private UserViewQueryRepository queryRepository;

    @Mock
    private CardNumberGenerator generator;

    @Mock
    private UserAggregateFactory aggregateFactory;
    
    @Mock
    private UserAggregate aggregate;

    private CardNumberMigrationService service;

    @BeforeEach
    void setup() {
        service = new CardNumberMigrationService(queryRepository, generator, aggregateFactory);
    }

    @Test
    void shouldAssignCardNumberToLegacyUser() {

        String userId = UUID.randomUUID().toString();

        UserView userView = new UserView(userId, MAIL, NAME, LASTNAME, UUID.randomUUID().toString(),
                null, UserStatus.ACTIVE, UserRole.READER);

        when(queryRepository.findWithoutCardNumber()).thenReturn(List.of(userView));

        when(generator.generate()).thenReturn(new CardNumber(CARD_NUMBER));

        when(aggregateFactory.create(userId)).thenReturn(aggregate);

        service.migrate();

        verify(aggregateFactory).create(userId);
        verify(generator).generate();
        verify(aggregate).assignCardNumber(CARD_NUMBER);
        
    }
    
    @Test
    void shouldDoNothingWhenNoUsersRequireMigration() {

        when(queryRepository.findWithoutCardNumber()).thenReturn(List.of());

        service.migrate();

        verify(generator, never()).generate();
        verify(aggregateFactory, never()).create(anyString());
        verify(aggregate, never()).assignCardNumber(anyString());
    }
}