package mentoring.acomi.bookservice.infrastructure.eventhandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.eventhandler.EventDispatcher;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookEventType;

@Component
public class SyncEventDispatcher implements EventDispatcher {

	private Map<BookEventType, List<Consumer<BookEvent>>> subscribers = new ConcurrentHashMap<>();

	private static final Logger logger = LogManager.getLogger(SyncEventDispatcher.class);;

	@Override
	public void dispatch(BookEvent event) {

		List<Consumer<BookEvent>> callbacks = subscribers.getOrDefault(event.type(), List.of());

		for (Consumer<BookEvent> callback : callbacks) {
			try {
				callback.accept(event);
			} catch (Exception e) {
				logger.error("[EventDispatcher] Subscriber failed, eventType={}", event.type(), e);
			}
		}

	}

	@Override
	public void subscribe(BookEventType eventType, Consumer<BookEvent> callback) {
		subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(callback);
	}

}
