package mentoring.acomi.loanservice.application.repositories;

import java.util.List;

import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;

public interface LoanEventRepository extends EventRepository<LoanEvent>{
		
	default List<LoanEvent> loadStream(String aggregateId) {
		return loadStream(aggregateId, AggregateType.LOAN.name());
	}
}
