package mentoring.acomi.loanservice.application.repositories;

import java.time.LocalDate;
import java.util.List;

import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;

public interface UserLoanStatisticQueryRepository {
	List<OverdueStatisticDto> getOverdueStatistics(LocalDate today);
}