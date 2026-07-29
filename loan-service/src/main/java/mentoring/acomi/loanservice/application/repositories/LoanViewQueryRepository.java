package mentoring.acomi.loanservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;

public interface LoanViewQueryRepository {
	 public Optional<LoanView> findById(String id);
	 public List<LoanDto> find(LoanFilter filter);
	 public List<LoanDto> getLoansOverdue();
}
