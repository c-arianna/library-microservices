package mentoring.acomi.loanservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.application.projection.LoanProjectionOperations;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.application.projection.UserProjectionOperations;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;

@Configuration
public class LoanProjectionConfiguration {

	@Bean("liveLoanProjection")
	LoanProjectionOperations liveLoanProjection(LoanViewRepository repository) {
		return new LoanProjection(repository);
	}

	@Bean("replayLoanProjection")
	LoanProjectionOperations replayLoanProjection(@Qualifier("replayRepo") LoanViewRepository repository) {
		return new LoanProjection(repository);
	}
	
	@Bean("liveUserProjection")
	UserProjectionOperations liveUserProjection(UserViewRepository repository) {
		return new UserProjection(repository);
	}

	@Bean("replayUserProjection")
	UserProjectionOperations replayUserProjection(@Qualifier("userReplayRepo") UserViewRepository repository) {
		return new UserProjection(repository);
	}
	
}
