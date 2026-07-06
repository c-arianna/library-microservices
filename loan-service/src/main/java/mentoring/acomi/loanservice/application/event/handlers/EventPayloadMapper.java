package mentoring.acomi.loanservice.application.event.handlers;

import org.springframework.stereotype.Component;

import jakarta.validation.Validator;
import mentoring.acomi.loanservice.application.errors.InvalidEventPayloadException;
import tools.jackson.databind.ObjectMapper;

@Component
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
            throw new InvalidEventPayloadException(violations.toString());
        }

        return result;
    }
}