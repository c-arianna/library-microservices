package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.JsonNode;

public abstract class AbstractNotificationHandler implements NotificationHandler {

	protected String requiredField(JsonNode payload, String fieldName) {

		String value = optionalField(payload, fieldName);

		if(value == null) {
			throw new NotificationHandlingException("Missing required field '%s'".formatted(fieldName));
		}
		
		if (value.isBlank()) {
			throw new NotificationHandlingException("Blank required field '%s'".formatted(fieldName));
		}

		return value;
	}

	protected String optionalField(JsonNode payload, String fieldName) {

	    JsonNode node = payload.get(fieldName);

	    if (node == null || node.isNull()) {
	        return null;
	    }

	    if (!node.isString()) {
	        throw new NotificationHandlingException("Field '%s' must be a string".formatted(fieldName));
	    }

	    return node.asString();
	}
	
	protected int requiredIntField(JsonNode payload, String fieldName) {

	    JsonNode node = requiredNumericField(payload, fieldName);

	    return node.asInt();
	}

	protected long requiredLongField(JsonNode payload, String fieldName) {

	    JsonNode node = requiredNumericField(payload, fieldName);

	    return node.asLong();
	}
	
	protected boolean requiredBooleanField(JsonNode payload, String fieldName) {

	    JsonNode node = payload.get(fieldName);

	    if (node == null || node.isNull()) {
	        throw new NotificationHandlingException("Missing required field '%s'".formatted(fieldName));
	    }

	    if (!node.isBoolean()) {
	        throw new NotificationHandlingException("Field '%s' must be a boolean".formatted(fieldName));
	    }

	    return node.asBoolean();
	}
	
	private JsonNode requiredNumericField(JsonNode payload, String fieldName) {
		
		JsonNode node = payload.get(fieldName);

	    if (node == null || node.isNull()) {
	        throw new NotificationHandlingException("Missing required field '%s'".formatted(fieldName));
	    }

	    if (!node.isNumber()) {
	        throw new NotificationHandlingException("Field '%s' must be a number".formatted(fieldName));
	    }
	    
		return node;
	}
	
}