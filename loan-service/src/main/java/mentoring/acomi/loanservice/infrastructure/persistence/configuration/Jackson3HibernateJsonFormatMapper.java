package mentoring.acomi.loanservice.infrastructure.persistence.configuration;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.AbstractJsonFormatMapper;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

public final class Jackson3HibernateJsonFormatMapper extends AbstractJsonFormatMapper {

    public static final String SHORT_NAME = "jackson";

    private final JsonMapper jsonMapper;

    public Jackson3HibernateJsonFormatMapper() {
        this(JsonMapper.builder().build());
    }

    public Jackson3HibernateJsonFormatMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected <T> T fromString(CharSequence charSequence, Type type) {
        try {
            return jsonMapper.readerFor(jsonMapper.constructType(type))
                    .readValue(charSequence.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize JSON", e);
        }
    }

    @Override
    protected <T> String toString(T value, Type type) {
        try {
            return jsonMapper.writerFor(jsonMapper.constructType(type))
                    .writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize JSON", e);
        }
    }

    @Override
    public <T> void writeToTarget(T value, JavaType<T> javaType, Object target, WrapperOptions options) {
        try {
            jsonMapper.writerFor(jsonMapper.constructType(javaType.getJavaType()))
                    .writeValue((JsonGenerator) target, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not write JSON to target", e);
        }
    }

    @Override
    public <T> T readFromSource(JavaType<T> javaType, Object source, WrapperOptions options) {
        try {
            return jsonMapper.readerFor(jsonMapper.constructType(javaType.getJavaType()))
                    .readValue((JsonParser) source);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read JSON from source", e);
        }
    }
}