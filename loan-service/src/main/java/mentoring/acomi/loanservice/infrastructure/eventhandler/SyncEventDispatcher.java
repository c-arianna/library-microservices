package mentoring.acomi.loanservice.infrastructure.eventhandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.eventhandler.EventDispatcher;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanEventType;

@Component
public class SyncEventDispatcher implements EventDispatcher {

	private Map<LoanEventType, List<Consumer<LoanEvent>>> subscribers = new ConcurrentHashMap<>();

	private static final Logger logger = LogManager.getLogger(SyncEventDispatcher.class);;

	@Override
	public void dispatch(LoanEvent event) {

		List<Consumer<LoanEvent>> callbacks = subscribers.getOrDefault(event.type(), List.of());

		for (Consumer<LoanEvent> callback : callbacks) {
			try {
				callback.accept(event);
			} catch (Exception e) {
				logger.error("[EventDispatcher] Subscriber failed, eventType={}", event.type(), e);
			}
		}

	}

	@Override
	public void subscribe(LoanEventType eventType, Consumer<LoanEvent> callback) {
		subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(callback);
	}

}
