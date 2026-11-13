package mentoring.acomi.loanservice.infrastructure.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.loanservice.application.dto.DailyLoanStatisticsDto;
import mentoring.acomi.loanservice.application.dto.LoanOverdueDto;
import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.application.dto.PopularBookDto;
import mentoring.acomi.loanservice.application.services.DashBoardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

	private final DashBoardService dashboardService;
	
	public DashboardController(DashBoardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("/overdue")
	public List<LoanOverdueDto> getLoansOverdue() {
		return dashboardService.getLoansOverdue();
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("/overdue/statistics")
	public List<OverdueStatisticDto> getOverdueStatistics() {
		return dashboardService.getOverdueStatistics();
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("/daily/statistics")
	public List<DailyLoanStatisticsDto> findDailyStatistics(@RequestParam(required = false) LocalDate from, 
			@RequestParam(required = false) LocalDate to) {
		return dashboardService.findDailyStatistics(from, to);
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("/popularBooks")
	public List<PopularBookDto> findMostPopularBooks(@RequestParam(defaultValue = "10") int limit) {
		return dashboardService.findMostPopularBooks(limit);
	}
}
