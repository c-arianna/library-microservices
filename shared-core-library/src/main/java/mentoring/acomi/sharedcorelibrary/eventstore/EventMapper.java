package mentoring.acomi.sharedcorelibrary.eventstore;

public interface EventMapper<E, ENTITY> {
	ENTITY toEntity(E event);
    E toDomain(ENTITY entity);
}
