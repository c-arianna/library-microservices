package mentoring.acomi.loanservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;

@Component
public class LoanProjection extends AbstractLoanProjection {

	public LoanProjection(LoanViewRepository repository) {
        super(repository);
    }
}