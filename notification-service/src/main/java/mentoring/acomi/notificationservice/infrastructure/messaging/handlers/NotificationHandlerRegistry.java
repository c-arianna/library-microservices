package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;

@Component
public class NotificationHandlerRegistry {

    private final Map<NotificationHandlerKey, NotificationHandler> handlers;

    public NotificationHandlerRegistry(List<NotificationHandler> handlers) {

        Map<NotificationHandlerKey, NotificationHandler> registry = new HashMap<>();

        for (NotificationHandler handler : handlers) {

        	NotificationHandlerMetadata metadata = AnnotationUtils.findAnnotation(AopUtils.getTargetClass(handler),
        			 NotificationHandlerMetadata.class);

            if (metadata == null) {
                throw new IllegalStateException("Missing @NotificationHandlerMetadata on %s".formatted(handler.getClass().getName()));
            }

            for (int version : metadata.supportedVersions()) {

                NotificationHandlerKey key = new NotificationHandlerKey(metadata.notificationEventType(), version);

                if (registry.containsKey(key)) {
                    throw new IllegalStateException("Duplicate handler for %s".formatted(key));
                }

                registry.put(key, handler);
            }
        }

        this.handlers = Map.copyOf(registry);
    }

    public Optional<NotificationHandler> find(NotificationEventEnvelope<?> event) {
        return Optional.ofNullable(handlers.get(new NotificationHandlerKey(event.eventType(), event.schemaVersion())));
    }
}