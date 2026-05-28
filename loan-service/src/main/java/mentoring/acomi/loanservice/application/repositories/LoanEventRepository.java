package mentoring.acomi.loanservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.loanservice.domain.events.LoanEvent;

public interface LoanEventRepository {
	public void appendToStream(LoanEvent event);
	public List<LoanEvent> loadStream(String aggregateId);
	public boolean exists(String aggregateId);
	public List<LoanEvent> loadAll();
	public Optional<LoanEvent> getEvent(String eventType, String aggregateId);
}
