package mentoring.acomi.notificationservice.messaging.handler;

import mentoring.acomi.notificationservice.infrastructure.messaging.AbstractNotificationHandler;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;

public class TestNotificationHandler extends AbstractNotificationHandler {

	@Override
    public IntegrationEventTypes eventType() {
        return IntegrationEventTypes.BOOK_REGISTERED;
    }

    @Override
    public boolean accepts(IntegrationEventEnvelope<?> event) {
        return true;
    }

    @Override
    public void handleEvent(IntegrationEventEnvelope<?> event) {
        
    }

    public String optionalField(JsonNode payload, String fieldName) {
    	return super.optionalField(payload, fieldName);
    }
    
    public String requiredField(JsonNode payload, String fieldName) {
        return super.requiredField(payload, fieldName);
    }

}
