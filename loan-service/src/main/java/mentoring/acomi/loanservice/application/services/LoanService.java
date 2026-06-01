package mentoring.acomi.loanservice.application.services;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.errors.ApplicationConflict;
import mentoring.acomi.loanservice.domain.errors.InvalidLoanStateTransition;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.model.Loan;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;
import mentoring.acomi.loanservice.infrastructure.dto.LoansResponse;
import mentoring.acomi.loanservice.infrastructure.security.GatewayPrincipal;

@Service
public class LoanService {

	private final LoanEventRepository loanEventRepository;
	private final LoanViewRepository loanViewRepository;
	private final EventDispatcher eventDispatcher;

	private final Logger logger = LogManager.getLogger(LoanService.class);

	public LoanService(LoanEventRepository eventRepository, LoanViewRepository loanViewRepository,
			EventDispatcher eventDispatcher) {
		this.loanEventRepository = eventRepository;
		this.loanViewRepository = loanViewRepository;
		this.eventDispatcher = eventDispatcher;
	}

	@Transactional
	public LoanResponse addLoan(AddLoanRequest request) {

		String loanId = UUID.randomUUID().toString();

		validateLoanRequest(request, loanId);

		Loan loan = Loan.create(loanId, request.isbn(), request.userId(), request.startDate(), request.endDate());

		LoanAggregate aggregate = loadLoan(loanId);
		aggregate.add(loan);

		return new LoanResponse(loanId);
	}

	@Transactional
	public void confirmLoan(String loanId) {

		LoanAggregate aggregate = loadLoan(loanId);

		aggregate.ensureCreated();

		if (!aggregate.isConfirmable()) {
			throw new InvalidLoanStateTransition("Cannot confirm loan");
		}

		aggregate.requestConfirm();

	}

	@Transactional
	public void cancelLoan(String loanId) {
		LoanAggregate aggregate = loadLoan(loanId);
		aggregate.cancel();
	}

	@Transactional
	public void returnLoan(String loanId) {
		LoanAggregate aggregate = loadLoan(loanId);
		aggregate.returnLoan();
	}

	public LoansResponse findLoans(LoanFilter filter) {

		LoanFilter finalFilter = applyCheckUserFilter(filter);
		
		List<LoanView> loans = loanViewRepository.find(finalFilter);
		return toLoansResponse(loans);
	}

	private void validateLoanRequest(AddLoanRequest request, String loanId) {

		if (loanEventRepository.exists(loanId)) {
			throw new ApplicationConflict("LOAN_ALREADY_EXISTS", String.format("Loan ID: %s", loanId));
		}

	}

	private LoansResponse toLoansResponse(List<LoanView> loans) {

		List<LoanDto> loanResponse = loans.stream().map(
				loan -> new LoanDto(loan.id(), loan.isbn(), loan.userId(), loan.status(), loan.start(), loan.end()))
				.toList();

		return new LoansResponse(loanResponse);
	}

	private LoanAggregate loadLoan(String loanId) {
		List<LoanEvent> events = loanEventRepository.loadStream(loanId);
		Consumer<LoanEvent> dispatch = event -> {
			loanEventRepository.appendToStream(event);
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new LoanAggregate(loanId, dispatch, events);
	}

	private LoanFilter applyCheckUserFilter(LoanFilter filter) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		GatewayPrincipal principal = (GatewayPrincipal) auth.getPrincipal();

		String role = principal.role();
		
		return "READER".equals(role) ? new LoanFilter(filter.isbn(), principal.userId(), filter.status()) : filter;

	}
}
