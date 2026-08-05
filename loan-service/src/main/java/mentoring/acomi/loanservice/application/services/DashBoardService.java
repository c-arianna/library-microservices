package mentoring.acomi.loanservice.application.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.dto.DailyLoanStatisticsDto;
import mentoring.acomi.loanservice.application.dto.LoanDto;
import mentoring.acomi.loanservice.application.dto.LoanOverdueDto;
import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.application.dto.PopularBookDto;
import mentoring.acomi.loanservice.application.errors.InvalidStatisticDateRange;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.PopularBookViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;
import mentoring.acomi.loanservice.application.view.PopularBookView;

@Service
public class DashBoardService {

	private final LoanViewQueryRepository repository;
    private final UserLoanStatisticQueryRepository userLoanStatisticRepository;
    private final DailyLoanStatisticQueryRepository dailyStatisticsRepository;
    private final PopularBookViewQueryRepository popularBookViewRepository;
    
	public DashBoardService(LoanViewQueryRepository repository, UserLoanStatisticQueryRepository userLoanStatisticRepository,
			DailyLoanStatisticQueryRepository dailyStatisticsRepository, PopularBookViewQueryRepository popularBookViewRepository) {
		this.repository = repository;
		this.userLoanStatisticRepository = userLoanStatisticRepository;
		this.dailyStatisticsRepository = dailyStatisticsRepository;
		this.popularBookViewRepository = popularBookViewRepository;
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
		
		return dailyStatisticsRepository.findStatistics(effectiveFrom, effectiveTo).stream().map(this::toDailyLoanStatisticsDto).toList();
	}
	
	private DailyLoanStatisticsDto toDailyLoanStatisticsDto(DailyLoanStatisticView view) {
		return new DailyLoanStatisticsDto(view.statisticsDate(), view.loansCreated(), view.loansConfirmed(), view.loansCanceled(),
				view.loansReturned());
	}

	public List<PopularBookDto> findMostPopularBooks(int limit) {
		limit = Math.min(Math.max(limit, 1), 100);
		return popularBookViewRepository.findMostPopularBooks(limit).stream().map(this::toPopularBookDto).toList();
	}
		
	private PopularBookDto toPopularBookDto(PopularBookView view) {
		return new PopularBookDto(view.isbn(), view.author(), view.title(), view.loanCount());
	}
}
