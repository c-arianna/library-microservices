package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.AbstractLoanProjection;

@Component
public class LoanProjectionReplay extends AbstractLoanProjection {

	public LoanProjectionReplay(@Qualifier("replayRepo") LoanViewReplayRepository repository) {
		super(repository);
	}

}
