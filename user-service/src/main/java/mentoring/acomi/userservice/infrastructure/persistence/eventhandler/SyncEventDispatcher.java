package mentoring.acomi.userservice.infrastructure.persistence.eventhandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.eventhandler.EventDispatcher;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;

@Component
public class SyncEventDispatcher implements EventDispatcher {

	private Map<UserEventType, List<Consumer<UserEvent>>> subscribers = new ConcurrentHashMap<>();

	private static final Logger logger = LogManager.getLogger(SyncEventDispatcher.class);;

	@Override
	public void dispatch(UserEvent event) {

		List<Consumer<UserEvent>> callbacks = subscribers.getOrDefault(event.type(), List.of());

		for (Consumer<UserEvent> callback : callbacks) {
			try {
				callback.accept(event);
			} catch (Exception e) {
				logger.error("[EventDispatcher] Subscriber failed, eventType={}", event.type(), e);
			}
		}

	}

	@Override
	public void subscribe(UserEventType eventType, Consumer<UserEvent> callback) {
		subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(callback);
	}

}
