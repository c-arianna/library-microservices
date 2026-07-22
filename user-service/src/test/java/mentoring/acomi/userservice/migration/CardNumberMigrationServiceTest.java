package mentoring.acomi.userservice.migration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.migration.CardNumberMigrationService;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.LibraryCardAssignedEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.domain.model.CardNumber;

@ExtendWith(MockitoExtension.class)
class CardNumberMigrationServiceTest {

    private static final String LASTNAME = "Potter";

	private static final String NAME = "Harry";

	private static final String MAIL = "test@gmail.com";

	private static final String CARD_NUMBER = "LIB-000001";

	@Mock
    private UserViewQueryRepository queryRepository;

    @Mock
    private CardNumberGenerator generator;

    @Mock
    private UserEventRepository userEventRepository;

    @Mock
    private EventDispatcher eventDispatcher;

    private CardNumberMigrationService service;

    @BeforeEach
    void setup() {
        service = new CardNumberMigrationService(queryRepository, userEventRepository, generator, eventDispatcher);
    }

    @Test
    void shouldAssignCardNumberToLegacyUser() {

        String userId = UUID.randomUUID().toString();

        UserView userView = new UserView(userId, MAIL, NAME, LASTNAME, UUID.randomUUID().toString(),
                null, UserStatus.ACTIVE, UserRole.READER);

        UserSubscribedPayload payload = new UserSubscribedPayload(userId, MAIL, NAME, LASTNAME,
                        UUID.randomUUID().toString(), null, UserStatus.ACTIVE, UserRole.READER);

        UserSubscribedEvent subscribedEvent = new UserSubscribedEvent(userId, UUID.randomUUID().toString(), 0, payload, Instant.now());

        when(queryRepository.findWithoutCardNumber()).thenReturn(List.of(userView));

        when(generator.generate()).thenReturn(new CardNumber(CARD_NUMBER));

        when(userEventRepository.loadStream(userId)).thenReturn(List.of(subscribedEvent));

        service.migrate();

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);

        verify(userEventRepository).appendToStream(captor.capture(), eq(1));

        UserEvent event = captor.getValue();

        Assertions.assertInstanceOf(LibraryCardAssignedEvent.class, event);

        LibraryCardAssignedEvent assignedEvent = (LibraryCardAssignedEvent) event;

        Assertions.assertAll(
                () -> Assertions.assertEquals(userId, assignedEvent.payload().userId()),
                () -> Assertions.assertEquals(CARD_NUMBER, assignedEvent.payload().cardNumber()));
    }
    
    @Test
    void shouldDoNothingWhenNoUsersRequireMigration() {

        when(queryRepository.findWithoutCardNumber()).thenReturn(List.of());

        service.migrate();

        verify(generator, never()).generate();

        verify(userEventRepository, never()).appendToStream(any(), anyInt());
    }
}