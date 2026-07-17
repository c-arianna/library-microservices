package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerKey;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

@Component
public class ReplayEventHandlerRegistry {

    private final Map<EventHandlerKey, EventHandler> handlers;

    public ReplayEventHandlerRegistry(ReplayLoanHandlerFactory factory) {

        Map<EventHandlerKey, EventHandler> registry = new HashMap<>();

        for (EventHandler handler : factory.createHandlers()) {

            HandlerMetadata metadata = handler.getClass().getAnnotation(HandlerMetadata.class);

            for (int version : metadata.supportedVersions()) {

                registry.put(new EventHandlerKey(metadata.eventType(), version), handler);
            }
        }

        this.handlers = Map.copyOf(registry);
    }

    public Optional<EventHandler> find(IntegrationEventEnvelope<?> event) {
        return Optional.ofNullable(handlers.get(new EventHandlerKey(event.eventType(), event.schemaVersion())));
    }
}