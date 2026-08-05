package mentoring.acomi.loanservice.application.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.aggregates.LoanAggregate;
import mentoring.acomi.loanservice.application.aggregates.LoanAggregateFactory;
import mentoring.acomi.loanservice.application.dto.AddLoanRequest;
import mentoring.acomi.loanservice.application.dto.BookDto;
import mentoring.acomi.loanservice.application.dto.LoanDetailDto;
import mentoring.acomi.loanservice.application.dto.LoanDto;
import mentoring.acomi.loanservice.application.dto.LoanResponse;
import mentoring.acomi.loanservice.application.dto.LoanUserDto;
import mentoring.acomi.loanservice.application.dto.LoansResponse;
import mentoring.acomi.loanservice.application.errors.InvalidUser;
import mentoring.acomi.loanservice.application.errors.LoanNotFound;
import mentoring.acomi.loanservice.application.errors.UserNotFound;
import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.domain.errors.ApplicationConflict;
import mentoring.acomi.loanservice.domain.errors.InvalidLoanStateTransition;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.model.Loan;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@Service
public class LoanService {

	private final LoanEventRepository loanEventRepository;
	private final LoanViewQueryRepository loanViewRepository;
	private final UserViewQueryRepository userViewRepository;
	private final BookViewRepository bookRepository;
	
	private final LoanAggregateFactory aggregateFactory;
	
	public LoanService(LoanEventRepository eventRepository, LoanViewQueryRepository loanViewRepository,
			UserViewQueryRepository userViewRepository, BookViewRepository bookRepository, LoanAggregateFactory aggregateFactory) {
		this.loanEventRepository = eventRepository;
		this.loanViewRepository = loanViewRepository;
		this.userViewRepository = userViewRepository;
		this.bookRepository = bookRepository;
		this.aggregateFactory = aggregateFactory;
	}

	@Transactional
	public LoanResponse addLoan(AddLoanRequest request) {

		String loanId = UUID.randomUUID().toString();
		String userId = resolveUserId(request);

		validateLoanRequest(loanId, userId);

		Loan loan = Loan.create(loanId, request.isbn(), userId, request.startDate(), request.endDate());

		LoanAggregate aggregate = aggregateFactory.create(loanId);
		aggregate.add(loan);

		return new LoanResponse(loanId);
	}

	@Transactional
	public void confirmLoan(String loanId) {

		LoanAggregate aggregate = aggregateFactory.create(loanId);

		aggregate.ensureCreated();

		if (!aggregate.isConfirmable()) {
			throw new InvalidLoanStateTransition("Cannot confirm loan");
		}

		aggregate.requestConfirm();

	}

	@Transactional
	public void cancelLoan(String loanId) {
		LoanAggregate aggregate = aggregateFactory.create(loanId);
		aggregate.cancel();
	}

	@Transactional
	public void returnLoan(String loanId, LocalDate returnedAt) {
		LoanAggregate aggregate = aggregateFactory.create(loanId);
		aggregate.returnLoan(returnedAt);
	}

	public LoansResponse findLoans(LoanFilter filter) {

		LoanFilter finalFilter = applyCheckUserFilter(filter);

		List<LoanDto> loans = loanViewRepository.find(finalFilter);
		return new LoansResponse(loans);
	}

	private void validateLoanRequest(String loanId, String userId) {

		if (loanEventRepository.exists(loanId, AggregateType.LOAN.name())) {
			throw new ApplicationConflict("LOAN_ALREADY_EXISTS", String.format("Loan ID: %s", loanId));
		}

		UserView user = userViewRepository.findById(userId)
				.orElseThrow(() -> new UserNotFound(String.format("User ID: %s", userId)));

		if (user.status() != UserStatus.ACTIVE) {
			throw new InvalidUser(
					String.format("User ID %s is not active, status: %s", userId, user.status().toString()));
		}

	}
	
	private LoanFilter applyCheckUserFilter(LoanFilter filter) {

		UserInfo userInfo = getUserInfo();

		String email = userInfo.email();

		if (userInfo.isReader) {
			UserView user = userViewRepository.findNotDisabledUserByEmail(email)
					.orElseThrow(() -> new UserNotFound(String.format("Mail: %s", email)));
			return new LoanFilter(filter.isbn(), user.id(), filter.cardNumber(), filter.status());
		}

		return filter;

	}

	record UserInfo(String userIdentityProvider, String email, boolean isReader) {

	}

	private UserInfo getUserInfo() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new InvalidUser("User not logged");
		}

		Jwt jwt = (Jwt) auth.getPrincipal();

		String userIdentityProviderId = jwt.getSubject();
		
		if (userIdentityProviderId == null) {
			throw new UserNotFound("userIdentityProviderId not present in token");
		}
		
		String email = jwt.getClaim("email");

		if (email == null) {
			throw new UserNotFound("Email not present in token");
		}

		boolean isReader = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_READER"));

		return new UserInfo(userIdentityProviderId, email, isReader);
	}

	public LoanDetailDto getLoan(String loanId) {

		Optional<LoanView> loan = loanViewRepository.findById(loanId);

		if (loan.isEmpty()) {
			throw new LoanNotFound(String.format("Loan %s not found", loanId));
		}

		LoanView loanView = loan.get();
		
		UserInfo userInfo = getUserInfo();

		String userIdentityProviderId = userInfo.userIdentityProvider();

		UserView user = userViewRepository.findById(loanView.userId()).orElseThrow(
				() -> new UserNotFound("User %s not found".formatted(loanView.userId())));
		
		if (userInfo.isReader) {
			
			if(!user.identityProviderId().equals(userIdentityProviderId)) {
				throw new LoanNotFound("User identity Provider ID %s cannot see loan %s, loan user identity provider ID: %s"
						.formatted(userIdentityProviderId, loanId, user.identityProviderId()));
			}
		}

		LoanUserDto loanUser = new LoanUserDto(user.id(), user.cardNumber());
		
		Optional<BookView> book = bookRepository.findByIsbn(loanView.isbn());
		
		String title = book.isEmpty() ? "" : book.get().title();
		String author = book.isEmpty() ? "" : book.get().author();

		BookDto bookDto = new BookDto(loanView.isbn(), author, title);
		
		LocalDate now = LocalDate.now();
		
		boolean overdue = loanView.status() == LoanStatus.CONFIRMED && now.isAfter(loanView.end());
		long overdueDays = overdue ? Math.max(0, ChronoUnit.DAYS.between(loanView.end(), now)) : 0;
		
		return new LoanDetailDto(loanView.id(), bookDto, loanView.status(), loanView.start(), loanView.end(), loanUser, overdue,
				overdueDays);
	}
	
	private String resolveUserId(AddLoanRequest request) {

		String requestUserId = request.userId();
		
	    UserInfo userInfo = getUserInfo();

	    if (userInfo.isReader()) {
	        String email = userInfo.email();
			UserView user = userViewRepository.findNotDisabledUserByEmail(email).orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));
			
			String loggedUserId = user.id();
			
			if(requestUserId!=null && !requestUserId.equalsIgnoreCase(loggedUserId)) {
				throw new InvalidUser(String.format("User ID request: %s, User ID logged: %s", requestUserId, user.id()));
			}
			
	        return user.id();
	    }

	    if (requestUserId == null || requestUserId.isBlank()) {
	        throw new InvalidUser("User ID is required");
	    }

	    return request.userId();
	}

}
