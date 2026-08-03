package mentoring.acomi.loanservice.projection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.LibraryCardAssignedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.projection.UserProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserProjectionTest {
   
	@Mock
    private UserViewRepository repository;

    private UserProjection projection;

    @BeforeEach
    void setUp() {
        projection = new UserProjection(repository);
    }

    @Test
    void shouldSupportUserSubscribedEvent() {
        Assertions.assertTrue(projection.supports(IntegrationEventTypes.USER_SUBSCRIBED));
    }

    @Test
    void shouldSupportUserSuspendedEvent() {
    	 Assertions.assertTrue(projection.supports(IntegrationEventTypes.USER_SUSPENDED));
    }

    @Test
    void shouldSupportUserUnsubscribedEvent() {
    	 Assertions.assertTrue(projection.supports(IntegrationEventTypes.USER_UNSUBSCRIBED));
    }

    @Test
    void shouldSupportUserUnsuspendedEvent() {
    	 Assertions.assertTrue(projection.supports(IntegrationEventTypes.USER_UNSUSPENDED));
    }

    @Test
    void shouldSupportLibraryCardAssignedEvent() {
    	 Assertions.assertTrue(projection.supports(IntegrationEventTypes.LIBRARY_CARD_ASSIGNED));
    }

    @Test
    void shouldNotSupportLoanEvents() {
    	 Assertions.assertFalse(projection.supports(IntegrationEventTypes.LOAN_REQUESTED));
    }

    @Test
    void shouldAddSubscribedUser() {

        Instant occurredAt = Instant.parse("2026-07-30T10:00:00Z");

        UserSubscribedIntegrationPayload payload = new UserSubscribedIntegrationPayload("user-1", "mario.rossi@mail.it", "Mario", "Rossi",
        		"identity-provider-id", null, UserStatus.ACTIVE, UserRole.READER);

        projection.project(new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED, "test",
                        "user-1", AggregateType.USER.name(), 1, occurredAt, 1, payload));

        ArgumentCaptor<UserView> captor = ArgumentCaptor.forClass(UserView.class);

        verify(repository).add(captor.capture(), eq(occurredAt));

        UserView inserted = captor.getValue();

        Assertions.assertEquals("user-1", inserted.id());
        Assertions.assertEquals("Mario", inserted.name());
        Assertions.assertEquals("Rossi", inserted.lastname());
        Assertions.assertEquals("mario.rossi@mail.it", inserted.email());
        Assertions.assertEquals(UserStatus.ACTIVE, inserted.status());
    }

    @Test
    void shouldSuspendUser() {

        Instant occurredAt = Instant.now();

        projection.project(userStatusEvent(IntegrationEventTypes.USER_SUSPENDED, "user-1", UserStatus.SUSPENDED, occurredAt));

        verify(repository).updateStatus("user-1", UserStatus.SUSPENDED, occurredAt);
    }

    @Test
    void shouldUnsubscribeUser() {

        Instant occurredAt = Instant.now();

        projection.project(userStatusEvent(IntegrationEventTypes.USER_UNSUBSCRIBED, "user-1", UserStatus.DISABLED, occurredAt));

        verify(repository).updateStatus("user-1", UserStatus.DISABLED, occurredAt);
    }

    @Test
    void shouldUnsuspendUser() {

        Instant occurredAt = Instant.now();

        projection.project(userStatusEvent(IntegrationEventTypes.USER_UNSUSPENDED, "user-1", UserStatus.ACTIVE, occurredAt));

        verify(repository).updateStatus("user-1", UserStatus.ACTIVE, occurredAt);
    }

    @Test
    void shouldAssignLibraryCardNumber() {

        Instant occurredAt = Instant.now();

        projection.project(new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LIBRARY_CARD_ASSIGNED,
                        "test", "user-1", AggregateType.USER.name(), 1, occurredAt, 1, 
                        new LibraryCardAssignedIntegrationPayload("user-1", "LIB-000001")));

        verify(repository).updateCardNumber("user-1", "LIB-000001", occurredAt);
    }

    private IntegrationEventEnvelope<UserIntegrationPayload> userStatusEvent(IntegrationEventTypes eventType, String userId,
            UserStatus status, Instant occurredAt) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), eventType, "test", userId, AggregateType.USER.name(), 1,
                occurredAt, 1, new UserIntegrationPayload(userId, status));
    }
}