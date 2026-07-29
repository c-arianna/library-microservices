package mentoring.acomi.loanservice.application.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;
import mentoring.acomi.loanservice.infrastructure.dto.LoanOverdueDto;

@Service
public class DashBoardService {

	private final LoanViewQueryRepository repository;

	public DashBoardService(LoanViewQueryRepository repository) {
		this.repository = repository;
	}
	
	public List<LoanOverdueDto> getLoansOverdue(){
		List<LoanDto> loansOverdue = repository.getLoansOverdue();
		return loansOverdue.stream().map(this::toLoanOverdue).toList();
	}
	
	public LoanOverdueDto toLoanOverdue(LoanDto loan) {
		long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(loan.endDate(), LocalDate.now()));
		return new LoanOverdueDto(loan.id(), loan.isbn(), loan.userId(), loan.cardNumber(), loan.endDate(), daysOverdue);
	}
	
}
