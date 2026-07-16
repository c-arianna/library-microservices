package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerMetadata {
	
	IntegrationEventTypes eventType();
	int[] supportedVersions() default {1};

}
