package mentoring.acomi.loanservice.application.repositories;

import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.sharedlibrary.eventstore.EventRepository;

public interface LoanEventRepository extends EventRepository<LoanEvent>{
}
