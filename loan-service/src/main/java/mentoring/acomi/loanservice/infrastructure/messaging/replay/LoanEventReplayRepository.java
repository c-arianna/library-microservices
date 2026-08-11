package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;

public interface LoanEventReplayRepository {
	List<LoanEventEntity> findAllEvents();
}
