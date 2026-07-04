package mentoring.acomi.loanservice.application.repositories;

import java.util.List;

import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;

public interface LoanEventRepository extends EventRepository<LoanEvent>{
	List<LoanEventEntity> findAllEvents();
}
