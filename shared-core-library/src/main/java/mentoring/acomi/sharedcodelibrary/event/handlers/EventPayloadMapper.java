package mentoring.acomi.sharedcodelibrary.event.handlers;

import jakarta.validation.Validator;
import tools.jackson.databind.ObjectMapper;

public class EventPayloadMapper {

    private final ObjectMapper mapper;
    private final Validator validator;

    public EventPayloadMapper(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T mapAndValidate(Object payload, Class<T> type) {

        T result = mapper.convertValue(payload, type);

        var violations = validator.validate(result);

        if (!violations.isEmpty()) {
            throw new InvalidEventPayloadException(violations);
        }

        return result;
    }
}