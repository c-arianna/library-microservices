package mentoring.acomi.loanservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

public interface LoanViewRepository {
	 public void insertRequest(LoanView loan);
	 public void updateStatus(String id, LoanStatus status);
	 public Optional<LoanView> findById(String id);
	 public List<LoanView> find(LoanFilter filter);
}
