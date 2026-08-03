package mentoring.acomi.loanservice.infrastructure.projection;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class DailyLoanStatisticProjection implements EventProjector {

	private final DailyLoanStatisticRepository repository;
		
	private final Map<IntegrationEventTypes, Consumer<LocalDate>> handlers;
	
	public DailyLoanStatisticProjection(DailyLoanStatisticRepository repository) {
		this.repository = repository;
		handlers = Map.of(IntegrationEventTypes.LOAN_REQUESTED, this::registerLoanCreated,
	            IntegrationEventTypes.LOAN_CONFIRMED, this::registerLoanConfirmed,
	            IntegrationEventTypes.LOAN_CANCELED, this::registerLoanCanceled,
	            IntegrationEventTypes.LOAN_RETURNED, this::registerLoanReturned);
	}

	@Override
    public boolean supports(IntegrationEventTypes type) {
        return handlers.containsKey(type);
    }

	@Override
	public void project(IntegrationEventEnvelope<?> event) {
		 handlers.get(event.eventType()).accept(getStatisticDate(event.occurredAt()));
	}
	
	private void registerLoanCreated(LocalDate statisticDate) {
		repository.registerLoanCreated(statisticDate);
	}

	private void registerLoanConfirmed(LocalDate statisticDate) {
		repository.registerLoanConfirmed(statisticDate);
	}

	private void registerLoanReturned(LocalDate statisticDate) {
		repository.registerLoanReturned(statisticDate);
	}

	private void registerLoanCanceled(LocalDate statisticDate) {
		repository.registerLoanCanceled(statisticDate);
	}
	
	private LocalDate getStatisticDate(Instant statisticInstant) {
		return  statisticInstant.atZone(ZoneId.of("Europe/Rome")).toLocalDate();
	}
}
