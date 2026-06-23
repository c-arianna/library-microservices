package mentoring.acomi.userservice.event;

import org.junit.jupiter.api.Assertions;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.config.SecurityTestConfig;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.payload.UserPayload;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
public class UserEventRepositoryTest {

	@Autowired
	private UserEventRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Test
	public void shouldAppendEventToStream() {

		String userId = UUID.randomUUID().toString();
		UserEvent event = createUserSubscribedEvent(userId);

		repository.appendToStream(event);

		entityManager.clear();

		List<UserEvent> events = repository.loadStream(userId);

		Assertions.assertEquals(1, events.size());
	}

	@Test
	public void shouldPreserveEventOrder() {

		String userId = UUID.randomUUID().toString();
		UserSubscribedEvent userSubscribedEvent = createUserSubscribedEvent(userId);
		repository.appendToStream(userSubscribedEvent);

		UserSuspendEvent userSuspenedEvent = createUserSuspendedEvent(userId);
		repository.appendToStream(userSuspenedEvent);

		entityManager.clear();

		List<UserEvent> events = repository.loadStream(userId);

		Assertions.assertEquals(2, events.size());

		Assertions.assertEquals(UserEventType.UserSubscribed, events.get(0).type());
		Assertions.assertEquals(UserEventType.UserSuspended, events.get(1).type());

	}

	@Test
	public void shouldReturnTrueIfAggregateExists() {

		String userId = UUID.randomUUID().toString();
		UserSubscribedEvent userSubscribedEvent = createUserSubscribedEvent(userId);
		repository.appendToStream(userSubscribedEvent);

		entityManager.clear();

		boolean exists = repository.exists(userId, AggregateType.USER.name());

		Assertions.assertTrue(exists);
	}

	@Test
	public void shouldReturnFalseIfAggregateDoesNotExist() {

		boolean exists = repository.exists("user01", AggregateType.USER.name());

		Assertions.assertFalse(exists);
	}

	@Test
	public void shouldReturnEvent() {

		String userId = UUID.randomUUID().toString();
		UserSubscribedEvent userSubscribedEvent = createUserSubscribedEvent(userId);
		repository.appendToStream(userSubscribedEvent);

		entityManager.clear();
		
		Optional<UserEvent> event = repository.getEvent(UserEventType.UserSubscribed.name(), userId);

		Assertions.assertTrue(event.isPresent());
	}

	@Test
	void shouldReturnEmptyIfEventDoesNotExist() {

		Optional<UserEvent> event = repository.getEvent("EVENT_UNKNOWN", "user0000001");

		Assertions.assertTrue(event.isEmpty());
	}

	@Test
	void shouldSerializeAndDeserializeEvent() {

		String userId = UUID.randomUUID().toString();
		UserSubscribedEvent userSubscribedEvent = createUserSubscribedEvent(userId);
		repository.appendToStream(userSubscribedEvent);

		List<UserEvent> loaded = repository.loadStream(userId);

		Assertions.assertEquals(userSubscribedEvent.type(), loaded.get(0).type());
	}

	private UserSubscribedEvent createUserSubscribedEvent(String userId) {
		String eventId = UUID.randomUUID().toString();
		String identityId = UUID.randomUUID().toString();
		UserSubscribedPayload payload = new UserSubscribedPayload(userId, "test@gmail.com", "Arianna", "Comi", identityId, UserStatus.ACTIVE, UserRole.READER);
		return new UserSubscribedEvent(userId, eventId, 0, payload, Instant.now());

	}

	private UserSuspendEvent createUserSuspendedEvent(String userId) {

		String eventId = UUID.randomUUID().toString();

		UserPayload payload = new UserPayload(userId, "test@gmail.com", "User not respect the loan deadline", "admin01");
		return new UserSuspendEvent(userId, eventId, 0, payload, Instant.now());

	}

}
