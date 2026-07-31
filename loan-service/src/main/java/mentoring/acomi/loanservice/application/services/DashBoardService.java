package mentoring.acomi.loanservice.application.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.dto.DailyLoanStatisticsDto;
import mentoring.acomi.loanservice.application.dto.LoanDto;
import mentoring.acomi.loanservice.application.dto.LoanOverdueDto;
import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.application.errors.InvalidStatisticDateRange;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;

@Service
public class DashBoardService {

	private final LoanViewQueryRepository repository;
    private final UserLoanStatisticQueryRepository userLoanStatisticRepository;
    private final DailyLoanStatisticQueryRepository dailyStatisticsRepository;
    
	public DashBoardService(LoanViewQueryRepository repository, UserLoanStatisticQueryRepository userLoanStatisticRepository,
			DailyLoanStatisticQueryRepository dailyStatisticsRepository) {
		this.repository = repository;
		this.userLoanStatisticRepository = userLoanStatisticRepository;
		this.dailyStatisticsRepository = dailyStatisticsRepository;
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
		return userLoanStatisticRepository.getOverdueStatistics(LocalDate.now());
	}

	public List<DailyLoanStatisticsDto> findDailyStatistics(LocalDate from, LocalDate to) {
		
		LocalDate effectiveTo = to != null ? to : LocalDate.now();
		LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
		 
		if (effectiveFrom.isAfter(effectiveTo)) {
		    throw new InvalidStatisticDateRange("from must be before to");
		}
		
		return dailyStatisticsRepository.findStatistics(effectiveFrom, effectiveTo).stream().map(this::DailyLoanStatisticsDto).toList();
	}
	
	private DailyLoanStatisticsDto DailyLoanStatisticsDto(DailyLoanStatisticView view) {
		return new DailyLoanStatisticsDto(view.statisticsDate(), view.loansCreated(), view.loansConfirmed(), view.loansCanceled(),
				view.loansReturned());
	}
		
}
