package mentoring.acomi.sharedlibrary.eventstore;

public interface EventMapper<E, ENTITY> {
	ENTITY toEntity(E event);
    E toDomain(ENTITY entity);
}
