package mentoring.acomi.loanservice.application.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.dto.LoanDto;
import mentoring.acomi.loanservice.application.dto.LoanOverdueDto;
import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticQueryRepository;

@Service
public class DashBoardService {

	private final LoanViewQueryRepository repository;
    private final UserLoanStatisticQueryRepository statisticRepository;
    
	public DashBoardService(LoanViewQueryRepository repository, UserLoanStatisticQueryRepository statisticRepository) {
		this.repository = repository;
		this.statisticRepository = statisticRepository;
	}
	
	public List<LoanOverdueDto> getLoansOverdue(){
		List<LoanDto> loansOverdue = repository.getLoansOverdue();
		return loansOverdue.stream().map(this::toLoanOverdue).toList();
	}
	
	public LoanOverdueDto toLoanOverdue(LoanDto loan) {
		long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(loan.endDate(), LocalDate.now()));
		return new LoanOverdueDto(loan.id(), loan.isbn(), loan.userId(), loan.cardNumber(), loan.endDate(), daysOverdue);
	}
	
	public List<OverdueStatisticDto> getOverdueStatistics() {
		return statisticRepository.getOverdueStatistics(LocalDate.now());
	}
		
}
