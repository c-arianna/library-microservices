package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotificationHandlerMetadata {
	NotificationEventType notificationEventType();
	int[] supportedVersions() default {1};
}
