package mentoring.acomi.userservice.application.aggregates;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import mentoring.acomi.userservice.domain.errors.UserNotExist;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserPayload;
import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;
import mentoring.acomi.userservice.domain.model.User;
import mentoring.acomi.userservice.domain.model.UserRole;
import mentoring.acomi.userservice.domain.model.UserStatus;

public class UserAggregate {

	private Consumer<UserEvent> dispatcher;

	private String id;
	private boolean isCreated = false;
	private String email;
	private UserStatus status;
	private UserRole role;

	public UserAggregate(String id, Consumer<UserEvent> dispatcher, List<UserEvent> events) {
		this.id = id;
		this.dispatcher = dispatcher;
		replay(events);
	}

	private void replay(List<UserEvent> events) {
		for (UserEvent event : events) {
			apply(event);
		}
	}

	public void apply(UserEvent event) {
		switch (event) {
		case UserSubscribedEvent e -> applyUserSubscribed(e);
		case UserUnsubscribeEvent e -> applyUserUnsubscribe(e);
		case UserSuspendEvent e -> applyUserSuspended(e);
		case UserUnsuspendedEvent e -> applyUserUnsuspended(e);
		}
	}

	private void applyUserSubscribed(UserSubscribedEvent event) {
		isCreated = true;
		status = UserStatus.ACTIVE;
		role = event.payload().role();
		email = event.payload().email();
	}

	private void applyUserUnsubscribe(UserUnsubscribeEvent event) {
		status = UserStatus.DISABLE;
	}

	private void applyUserSuspended(UserSuspendEvent event) {
		status = UserStatus.SUSPENDED;
	}

	private void applyUserUnsuspended(UserUnsuspendedEvent event) {
		status = UserStatus.ACTIVE;
	}
	
	public void subscribe(User user) {

		if (!isCreated) {
			UserSubscribedPayload payload = new UserSubscribedPayload(user.getId(), user.getEmail().getValue(),
					user.getName(), user.getLastname(), user.getPassword().value(), user.getStatus(), user.getRole());
			UserSubscribedEvent event = new UserSubscribedEvent(id, getEventId(), payload, Instant.now());
			manageEvent(event);
		}
	}

	public void unsubscribe(String reason) {

		ensureCreated();
		
		if(UserStatus.ACTIVE.equals(status)) {
			UserUnsubscribedPayload payload = new UserUnsubscribedPayload(id, email, reason);
			UserUnsubscribeEvent event = new UserUnsubscribeEvent(id, getEventId(), payload, Instant.now());
			manageEvent(event);
		}
	}

	public void suspend(String reason, String suspendedBy) {
		
		ensureCreated();
		
		if(UserStatus.ACTIVE.equals(status)) {
			UserPayload payload = new UserPayload(id, email, reason, suspendedBy);
			UserSuspendEvent event = new UserSuspendEvent(id, getEventId(), payload, Instant.now());
			manageEvent(event);
		}
		
	}
	
	public void unsuspend(String reason, String suspendedBy) {
		
		ensureCreated();
		
		if(UserStatus.SUSPENDED.equals(status)) {
			UserPayload payload = new UserPayload(id, email, reason, suspendedBy);
			UserUnsuspendedEvent event = new UserUnsuspendedEvent(id, getEventId(), payload, Instant.now());
			manageEvent(event);
		}
		
	}
	
	private String getEventId() {
		return UUID.randomUUID().toString();
	}

	private void manageEvent(UserEvent event) {
		apply(event);
		dispatcher.accept(event);
	}

	public String email() {
		return email;
	}
		
	public UserRole role() {
		return role;
	}
	
	private void ensureCreated() {
		if (!isCreated) {
			throw new UserNotExist(String.format("User not exists: %s", id));
		}

	}

}
