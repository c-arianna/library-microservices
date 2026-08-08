package mentoring.acomi.bookservice.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.aggregates.BookRequestAggregate;
import mentoring.acomi.bookservice.application.aggregates.BookRequestAggregateFactory;
import mentoring.acomi.bookservice.application.dto.BookRequestAddedResponse;
import mentoring.acomi.bookservice.application.dto.BookRequestDetailDto;
import mentoring.acomi.bookservice.application.dto.BookRequestDto;
import mentoring.acomi.bookservice.application.dto.BookRequestRejectDto;
import mentoring.acomi.bookservice.application.dto.BookRequestVoteDto;
import mentoring.acomi.bookservice.application.errors.BookAlreadyRegistered;
import mentoring.acomi.bookservice.application.errors.BookRequestAlreadyExists;
import mentoring.acomi.bookservice.application.errors.BookRequestNotFound;
import mentoring.acomi.bookservice.application.errors.InvalidUser;
import mentoring.acomi.bookservice.application.errors.UserNotFound;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.application.view.UserView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequest;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.domain.errors.InvalidBookRequestStateTransition;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequestDto;

@Service
public class BookRequestService {

	private final BookRequestAggregateFactory aggregateFactory;
	private final UserViewQueryRepository userViewRepository;
	private final BookViewQueryRepository bookQueryRepository;
	private final BookRequestViewQueryRepository bookRequestQueryRepository;
	private final BookRequestVoteViewQueryRepository bookRequestVoteQueryRepository;
	

	public BookRequestService(BookRequestAggregateFactory aggregateFactory, UserViewQueryRepository userViewRepository,
			BookViewQueryRepository bookQueryRepository, BookRequestViewQueryRepository bookRequestQueryRepository,
			BookRequestVoteViewQueryRepository bookRequestVoteQueryRepository) {
		this.aggregateFactory = aggregateFactory;
		this.userViewRepository = userViewRepository;
		this.bookQueryRepository = bookQueryRepository;
		this.bookRequestQueryRepository = bookRequestQueryRepository;
		this.bookRequestVoteQueryRepository = bookRequestVoteQueryRepository;
	}

	@Transactional
	public BookRequestAddedResponse addRequest(AddBookRequestDto request) {

		validateRequest(request);

		String requestId = UUID.randomUUID().toString();
		String userId = resolveUserId();

		BookRequest bookRequest = BookRequest.create(requestId, request.author(), request.title(), request.isbn(), userId, request.notes());
		BookRequestAggregate aggregate = aggregateFactory.create(requestId);
		aggregate.add(bookRequest);
		
		return new BookRequestAddedResponse(requestId);
	}

	public List<BookRequestDto> getBookRequests() {
		return bookRequestQueryRepository.findBookRequests().stream().map(this::toBookRequestDto).toList();
	}
	
	@Transactional
	public void approveBookRequest(String bookRequestId) {
		BookRequestAggregate aggregate = aggregateFactory.create(bookRequestId);
		
		aggregate.ensureCreated();
		
		if (!aggregate.isPending()) {
			throw new InvalidBookRequestStateTransition("Cannot approve book request");
		}
		aggregate.approve();
	}
	
	@Transactional	
	public void rejectBookRequest(BookRequestRejectDto request, String bookRequestId) {	
		BookRequestAggregate aggregate = aggregateFactory.create(bookRequestId);
		
		aggregate.ensureCreated();
		
		if (!aggregate.isPending()) {
			throw new InvalidBookRequestStateTransition("Cannot approve book request");
		}
		aggregate.reject(request.reason());
	}
	
	@Transactional
	public void voteBookRequest(String bookRequestId) {
		BookRequestAggregate aggregate = aggregateFactory.create(bookRequestId);
		String userId = resolveUserId();
		aggregate.vote(userId);
	}
	
	public BookRequestDetailDto getBookRequestDetail(String bookRequestId) {
		
		Optional<BookRequestView> found = bookRequestQueryRepository.findById(bookRequestId);
		
		if (found.isEmpty()) {
			throw new BookRequestNotFound(String.format("Book request %s not found", bookRequestId));
		}
		
		BookRequestView bookRequest = found.get();
		
		Optional<UserView> user = userViewRepository.findById(bookRequest.requesterUserId());
		
		String cardNumber = user.isEmpty() ? "" : user.get().cardNumber();
		
		List<BookRequestVoteDto> bookRequestVotes = bookRequestVoteQueryRepository.findVotesByRequestId(bookRequestId);
		
		return new BookRequestDetailDto(bookRequest.requestId(), bookRequest.requesterUserId(), cardNumber, bookRequest.isbn(), 
				bookRequest.author(), bookRequest.title(), bookRequest.notes(), bookRequest.status(), bookRequest.votes(), 
				canVote(bookRequest, bookRequestVotes), bookRequestVotes);
		
	}
	
	private BookRequestDto toBookRequestDto(BookRequestView view) {
		return new BookRequestDto(view.requestId(), view.requesterUserId(), view.author(), view.title(), view.isbn(), view.votes(), view.status());
	}
	
	private void validateRequest(AddBookRequestDto request) {

		String isbn = request.isbn();

		if (StringUtils.hasText(isbn)) {
			checkIsbn(isbn);
		}

		checkTitleAndAuthor(request.title(), request.author());

	}

	private void checkTitleAndAuthor(String title, String author) {
		
		String authorSearch = normalize(author);
		String titleSearch = normalize(title);

		BookFilter filter = new BookFilter(titleSearch, authorSearch, null, false);
		List<BookView> foundBook = bookQueryRepository.find(filter);

		if (!foundBook.isEmpty()) {
			throw new BookAlreadyRegistered("Found book in catalog with author %s and title %s".formatted(authorSearch, titleSearch));
		}

		Optional<BookRequestView> foundBookRequest = bookRequestQueryRepository.findPendingRequestByAuthorAndTitle(authorSearch, titleSearch);

		if (foundBookRequest.isPresent()) {
			throw new BookRequestAlreadyExists("Found request for author %s and title".formatted(authorSearch, titleSearch));
		}
	}

	private void checkIsbn(String isbn) {

		Optional<BookView> book = bookQueryRepository.findById(isbn);

		if (book.isPresent()) {
			throw new BookAlreadyRegistered("ISBN %s found in catalog".formatted(isbn));
		}

		Optional<BookRequestView> bookRequest = bookRequestQueryRepository.findPendingRequestByIsbn(isbn);

		if (bookRequest.isPresent()) {
			throw new BookRequestAlreadyExists("Found request for ISBN %s".formatted(isbn));
		}

	}

	private String normalize(String value) {
		return value.trim().replaceAll("\\s+", " ");
	}

	private String resolveUserId() {

		UserInfo userInfo = getUserInfo();

		String email = userInfo.email;
		
		UserView user = userViewRepository.findNotDisabledUserByEmail(email)
				.orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));

		return user.id();
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
	
	record UserInfo(String email, boolean isReader) {

	}
	
	private boolean canVote(BookRequestView request, List<BookRequestVoteDto> votes) {

	    if (request.status() != BookRequestStatus.PENDING) {
	        return false;
	    }

	    UserInfo userInfo = getUserInfo();
	    
	    if(userInfo.isReader) {
	    
	    	String email = userInfo.email;
			
			UserView user = userViewRepository.findNotDisabledUserByEmail(email)
					.orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));
			
		    String currentUserId = user.id();
		    
			if (request.requesterUserId().equals(currentUserId)) {
		        return false;
		    }

	       return votes.stream().noneMatch(vote -> vote.userId().equals(currentUserId));
	       
	    }
	    
	    return false;
	}

}
