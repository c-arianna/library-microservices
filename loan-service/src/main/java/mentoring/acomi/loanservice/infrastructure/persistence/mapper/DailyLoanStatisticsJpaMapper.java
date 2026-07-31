package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.DailyLoanStatisticEntity;

@Component
public class DailyLoanStatisticsJpaMapper {
	
	public DailyLoanStatisticEntity toEntity(DailyLoanStatisticView view) {
		return new DailyLoanStatisticEntity(view.statisticsDate(), view.loansCreated(), view.loansConfirmed(), view.loansCanceled(), 
				view.loansReturned());
	}
	
	public DailyLoanStatisticView toDomain(DailyLoanStatisticEntity entity) {
		return new DailyLoanStatisticView(entity.getStatisticsDate(), entity.getLoansCreated(), entity.getLoansConfirmed(), 
				entity.getLoansCanceled(), entity.getLoansReturned());
	}

}
