package mentoring.acomi.notificationservice.infrastructure.messaging;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import tools.jackson.databind.JsonNode;

public abstract class AbstractNotificationHandler implements EventHandler {

	protected String requiredField(JsonNode payload, String fieldName) {

		String value = optionalField(payload, fieldName);

		if (value.isBlank()) {
			throw new NotificationHandlingException("Blank required field '%s'".formatted(fieldName));
		}

		return value;
	}

	protected String optionalField(JsonNode payload, String fieldName) {

		JsonNode node = payload.get(fieldName);

		if (node == null || node.isNull()) {
			throw new NotificationHandlingException("Missing required field '%s'".formatted(fieldName));
		}

		if (!node.isString()) {
			throw new NotificationHandlingException("Field '%s' must be a string".formatted(fieldName));
		}

		return node.asString();
	}
	
	protected int requiredLongField(JsonNode payload, String fieldName) {

	    JsonNode node = payload.get(fieldName);

	    if (node == null || node.isNull()) {
	        throw new NotificationHandlingException("Missing required field '%s'".formatted(fieldName));
	    }

	    if (!node.isNumber()) {
	        throw new NotificationHandlingException("Field '%s' must be a number".formatted(fieldName));
	    }

	    return node.asInt();
	}
}