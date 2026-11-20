package mentoring.acomi.assistantservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.BookUpdatedNotificationPayload;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.LoanUpdatedNotificationPayload;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.UserUpdatedNotificationPayload;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.AssistantEventEntity;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import tools.jackson.databind.ObjectMapper;

@Component
public class AssistantNotificationEventJpaMapper implements EventMapper<NotificationEventEnvelope<?>, AssistantEventEntity> {

    private final ObjectMapper objectMapper;
    private final EventPayloadMapper payloadMapper;

    public AssistantNotificationEventJpaMapper(ObjectMapper objectMapper, EventPayloadMapper payloadMapper) {
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public AssistantEventEntity toEntity(NotificationEventEnvelope<?> event) {

        return new AssistantEventEntity(
                extractAggregateId(event),
                extractAggregateType(event),
                event.eventType().name(),
                event.eventId(),
                null,
                event.schemaVersion(),
                objectMapper.valueToTree(event.payload()),
                event.occurredAt());
    }

    private String extractAggregateId(NotificationEventEnvelope<?> event) {

        return switch (event.eventType()) {

            case BOOK_UPDATED -> {
                BookUpdatedNotificationPayload payload =
                        payloadMapper.mapAndValidate(
                                event.payload(),
                                BookUpdatedNotificationPayload.class);

                yield payload.isbn();
            }

            case USER_UPDATED -> {
                UserUpdatedNotificationPayload payload =
                        payloadMapper.mapAndValidate(
                                event.payload(),
                                UserUpdatedNotificationPayload.class);

                yield payload.userId();
            }

            case LOAN_UPDATED -> {
                LoanUpdatedNotificationPayload payload =
                        payloadMapper.mapAndValidate(
                                event.payload(),
                                LoanUpdatedNotificationPayload.class);

                yield payload.loanId();
            }
		default -> throw new IllegalArgumentException(String.format("Unexpected value: %s", event.eventType()));
        };
    }

    private String extractAggregateType(NotificationEventEnvelope<?> event) {

        return switch (event.eventType()) {

            case BOOK_UPDATED -> "BOOK";
            case USER_UPDATED -> "USER";
            case LOAN_UPDATED -> "LOAN";
            
		    default -> throw new IllegalArgumentException(String.format("Unexpected value: %s", event.eventType()));
        };
    }

	@Override
	public NotificationEventEnvelope<?> toDomain(AssistantEventEntity entity) {
		throw new UnsupportedOperationException("Not needed");
	}
}