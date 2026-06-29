package mentoring.acomi.loanservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

public interface LoanViewRepository {
	 public void insertRequest(LoanView loan, Instant createdAt);
	 public void updateStatus(String id, LoanStatus status, Instant updatedAt);
	 public void deleteAll();
}
