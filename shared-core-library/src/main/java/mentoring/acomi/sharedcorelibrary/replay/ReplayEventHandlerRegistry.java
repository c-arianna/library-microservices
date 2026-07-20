package mentoring.acomi.sharedcorelibrary.replay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerKey;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public class ReplayEventHandlerRegistry {

    private final Map<EventHandlerKey, EventHandler> handlers;

    public ReplayEventHandlerRegistry(List<EventHandler> handlers) {

        Map<EventHandlerKey, EventHandler> registry = new HashMap<>();

        for (EventHandler handler : handlers) {
            HandlerMetadata metadata = handler.getClass().getAnnotation(HandlerMetadata.class);

            if (metadata == null) {
                throw new IllegalStateException("Missing @HandlerMetadata on %s".formatted(handler.getClass().getName()));
            }

            for (int version : metadata.supportedVersions()) {

                EventHandlerKey key = new EventHandlerKey(metadata.eventType(), version);

                if(registry.containsKey(key)) {
                	 throw new IllegalStateException("Duplicate replay handler for %s".formatted(key));
                }
                
                registry.put(key, handler);
            }
        }

        this.handlers = Map.copyOf(registry);
    }

    public Optional<EventHandler> find(IntegrationEventEnvelope<?> event) {
        return Optional.ofNullable(handlers.get(new EventHandlerKey(event.eventType(), event.schemaVersion())));
    }

    public int size() {
        return handlers.size();
    }
}