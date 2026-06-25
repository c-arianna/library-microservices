package mentoring.acomi.loanservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.view.LoanView;

public interface LoanViewQueryRepository {
	 public Optional<LoanView> findById(String id);
	 public List<LoanView> find(LoanFilter filter);

}
