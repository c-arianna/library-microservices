package mentoring.acomi.assistantservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import mentoring.acomi.assistantservice.application.view.ProjectionView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.ProjectionEntity;
import mentoring.acomi.assistantservice.infrastructure.persistence.mapper.ViewMapper;

public abstract class AbstractJpaProjectionRepositoryAdapter<V extends ProjectionView<ID>, E extends ProjectionEntity, ID> {

	private final JpaRepository<E, ID> repository;
	protected final ViewMapper<V, E> mapper;

	protected AbstractJpaProjectionRepositoryAdapter(JpaRepository<E, ID> repository, ViewMapper<V, E> mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public void upsert(V view, Instant occurredAt) {

		repository.findById(view.id()).ifPresent(existing -> {

            if (existing.getUpdatedAt() != null && existing.getUpdatedAt().isAfter(occurredAt)) {
            	return;
            }
        });

		E entity = mapper.toEntity(view);

		entity.markUpdated(occurredAt);

		repository.save(entity);
	}
	
	
}