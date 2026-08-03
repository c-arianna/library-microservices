package mentoring.acomi.loanservice.infrastructure.projection;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class UserLoanStatisticProjection implements EventProjector {

	private final UserLoanStatisticRepository statisticRepository;
	private final LoanViewQueryRepository loanRepository;
	
	public UserLoanStatisticProjection(UserLoanStatisticRepository statisticRepository, LoanViewQueryRepository loanRepository) {
		this.statisticRepository = statisticRepository;
		this.loanRepository = loanRepository;
	}

	@Override
	public boolean supports(IntegrationEventTypes eventType) {
		return eventType == IntegrationEventTypes.LOAN_RETURNED;
	}

	@Override
	public void project(IntegrationEventEnvelope<?> event) {
		
		if(event.eventType() == IntegrationEventTypes.LOAN_RETURNED) {
			LoanReturnedIntegrationPayload payload = (LoanReturnedIntegrationPayload) event.payload();
			LocalDate returnedAt = event.schemaVersion() == 1 ? event.occurredAt().atZone(ZoneOffset.UTC).toLocalDate() : payload.returnedAt();
			registerOverdueLoan(payload.userId(), payload.loanId(), returnedAt);
		}
		
	}

	private void registerOverdueLoan(String userId, String loanId, LocalDate returnedAt) {

		LoanView loanView = loanRepository.findById(loanId).orElseThrow();
		LocalDate dueDate = loanView.end();

		if (!returnedAt.isAfter(dueDate)) {
			return;
		}

		Optional<UserLoanStatisticView> statistic = statisticRepository.getUserLoanStatistic(userId);

		long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnedAt);

		if (statistic.isEmpty()) {
			UserLoanStatisticView view = new UserLoanStatisticView(userId, 1, daysOverdue, returnedAt);
			statisticRepository.insert(view);
		} else {
			statisticRepository.statisticUpdate(userId, daysOverdue, returnedAt);
		}

	}

}
