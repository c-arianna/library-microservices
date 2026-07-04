package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class HandlerManager {
	
	Map<IntegrationEventTypes, List<EventHandler>> handlers = new HashMap<>();
	
	public void addHandler(IntegrationEventTypes eventType, EventHandler handler) {
		
		if(!handlers.containsKey(eventType)){
			handlers.put(eventType, new ArrayList<>());
		}
		
		handlers.get(eventType).add(handler);
	}
	
	public void handleEvent(IntegrationEventEnvelope<?> event) {
	
		if(handlers.containsKey(event.eventType())) {
			handlers.get(event.eventType()).stream().filter(h -> h.accepts(event)).forEach(h -> h.handleEvent(event));
		}
	}

}
