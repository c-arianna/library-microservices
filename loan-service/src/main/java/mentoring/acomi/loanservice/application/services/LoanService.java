package mentoring.acomi.loanservice.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.errors.InvalidUser;
import mentoring.acomi.loanservice.application.errors.LoanNotFound;
import mentoring.acomi.loanservice.application.errors.UserNotFound;
import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.domain.errors.ApplicationConflict;
import mentoring.acomi.loanservice.domain.errors.InvalidLoanStateTransition;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.model.Loan;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;
import mentoring.acomi.loanservice.infrastructure.dto.LoansResponse;
import mentoring.acomi.sharedlibrary.model.UserStatus;

@Service
public class LoanService {

	private final LoanEventRepository loanEventRepository;
	private final LoanViewQueryRepository loanViewRepository;
	private final UserViewQueryRepository userViewRepository;
	private final EventDispatcher eventDispatcher;

	private final Logger logger = LogManager.getLogger(LoanService.class);

	public LoanService(LoanEventRepository eventRepository, LoanViewQueryRepository loanViewRepository,
			UserViewQueryRepository userViewRepository, EventDispatcher eventDispatcher) {
		this.loanEventRepository = eventRepository;
		this.loanViewRepository = loanViewRepository;
		this.userViewRepository = userViewRepository;
		this.eventDispatcher = eventDispatcher;
	}

	@Transactional
	public LoanResponse addLoan(AddLoanRequest request) {

		String loanId = UUID.randomUUID().toString();
		String userId = request.userId();

		validateLoanRequest(loanId, userId);

		Loan loan = Loan.create(loanId, request.isbn(), userId, request.startDate(), request.endDate());

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

	private void validateLoanRequest(String loanId, String userId) {

		if (loanEventRepository.exists(loanId, AggregateType.LOAN.name())) {
			throw new ApplicationConflict("LOAN_ALREADY_EXISTS", String.format("Loan ID: %s", loanId));
		}

		UserInfo userInfo = getUserInfo();

		String email = userInfo.email();

		if (userInfo.isReader()) {

			UserView user = userViewRepository.findByEmail(email)
					.orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));

			String loggedUserId = user.id();
			if (!loggedUserId.equals(userId)) {
				throw new InvalidUser(String.format("User ID request: %s, User ID logged: %s", userId, loggedUserId));
			}
		}

		UserView user = userViewRepository.findById(userId)
				.orElseThrow(() -> new UserNotFound(String.format("User ID: %s", userId)));

		if (user.status() != UserStatus.ACTIVE) {
			throw new InvalidUser(
					String.format("User ID %s is not active, status: %s", userId, user.status().toString()));
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

		UserInfo userInfo = getUserInfo();

		String email = userInfo.email();

		if (userInfo.isReader) {
			UserView user = userViewRepository.findByEmail(email)
					.orElseThrow(() -> new UserNotFound(String.format("Mail: %s", email)));
			return new LoanFilter(filter.isbn(), user.id(), filter.status());
		}

		return filter;

	}

	record UserInfo(String email, boolean isReader) {

	}

	private UserInfo getUserInfo() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new InvalidUser("User not logged");
		}

		Jwt jwt = (Jwt) auth.getPrincipal();

		String email = jwt.getClaim("email");

		if (email == null) {
			throw new UserNotFound("Email not present in token");
		}

		boolean isReader = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_READER"));

		return new UserInfo(email, isReader);
	}

	public LoanDto getLoan(String loanId) {

		Optional<LoanView> loan = loanViewRepository.findById(loanId);

		if (loan.isEmpty()) {
			throw new LoanNotFound(String.format("Loan %s not found", loanId));
		}

		LoanView loanView = loan.get();
		
		UserInfo userInfo = getUserInfo();

		String email = userInfo.email();

		if (userInfo.isReader) {
			UserView user = userViewRepository.findByEmail(email).orElseThrow(() -> new UserNotFound(String.format("Mail: %s", email)));

			if(!loanView.userId().equalsIgnoreCase(user.id())) {
				throw new LoanNotFound("User %s cannot see loan %s, loan user ID: %s".formatted(user.id(), loanId, loanView.userId()));
			}
		}

		return new LoanDto(loanView.id(), loanView.isbn(), loanView.userId(), loanView.status(), loanView.start(), loanView.end());
	}

}
