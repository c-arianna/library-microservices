package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
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

	    JsonNode node = payload.get(fieldName);

	    if (node == null || node.isNull()) {
	        throw new NotificationHandlingException("Missing required field '%s'".formatted(fieldName));
	    }

	    if (!node.isNumber()) {
	        throw new NotificationHandlingException("Field '%s' must be a number".formatted(fieldName));
	    }

	    return node.asInt();
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
}