package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserLoanStatisticEntity;


@Component
public class UserLoanStatisticJpaMapper {

	public UserLoanStatisticEntity toEntity(UserLoanStatisticView view) {
		return new UserLoanStatisticEntity(view.userId(), view.overdueLoansCount(), view.totalDaysOverdue(), view.lastOverdueDate());
	}
	
	public UserLoanStatisticView toDomain(UserLoanStatisticEntity entity) {
		return new UserLoanStatisticView(entity.getUserId(), entity.getOverdueLoansCount(), entity.getTotalDaysOverdue(), 
				entity.getLastOverdueDate());
	}
}
