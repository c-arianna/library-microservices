package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;

@Component
public class LoanViewJpaMapper {

	public LoanViewEntity toEntity(LoanView loan) {
		return new LoanViewEntity(loan.id(), loan.isbn(), loan.userId(), loan.start(), loan.end());
	}

	public LoanView toView(LoanViewEntity entity) {

		if (entity == null) {
			return null;
		}

		return new LoanView(entity.getId(),entity.getIsbn(), entity.getUserId(), entity.getStartDate(), entity.getEndDate(), entity.getStatus());
	}
}
