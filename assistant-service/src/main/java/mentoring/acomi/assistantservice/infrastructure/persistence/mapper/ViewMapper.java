package mentoring.acomi.assistantservice.infrastructure.persistence.mapper;

public interface ViewMapper<V, E> {
    E toEntity(V view);
    V toView(E entity);
}