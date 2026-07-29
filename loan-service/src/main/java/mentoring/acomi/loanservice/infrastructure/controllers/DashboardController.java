package mentoring.acomi.loanservice.infrastructure.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.loanservice.application.services.DashBoardService;
import mentoring.acomi.loanservice.infrastructure.dto.LoanOverdueDto;

@RestController
public class DashboardController {

	private final DashBoardService dashboardService;
	
	public DashboardController(DashBoardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@GetMapping("dashboard/overdue")
	public List<LoanOverdueDto> getLoansOverdue() {
		return dashboardService.getLoansOverdue();
	}
}
