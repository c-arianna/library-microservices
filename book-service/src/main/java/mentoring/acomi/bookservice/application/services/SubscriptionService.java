package mentoring.acomi.bookservice.application.services;

import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.errors.InvalidUser;
import mentoring.acomi.bookservice.application.errors.UserNotFound;
import mentoring.acomi.bookservice.application.repositories.BookSubscriptionRepository;
import mentoring.acomi.bookservice.application.view.BookSubscriptionView;
import mentoring.acomi.bookservice.infrastructure.dto.AddSubscriptionRequest;
import mentoring.acomi.bookservice.infrastructure.dto.BookSubscriptionDto;
import mentoring.acomi.bookservice.infrastructure.dto.BookSubscriptionsResponse;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.SubscriptionType;

@Service
public class SubscriptionService {

	private final BookSubscriptionRepository bookSubscriptionRepository;
	
	public SubscriptionService(BookSubscriptionRepository bookSubscriptionRepository) {
		this.bookSubscriptionRepository = bookSubscriptionRepository;
	}

	public void addSubscription(AddSubscriptionRequest request, String isbn) {
		String userIdentityId = getUserIdentityId();
		bookSubscriptionRepository.add(new BookSubscriptionView(null, isbn, userIdentityId, request.phoneNumber(), BookSubscriptionStatus.ACTIVE,
			        SubscriptionType.BOOK_AVAILABLE, Instant.now(), null));		
	}
	
	private String getUserIdentityId() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new InvalidUser("User not logged");
		}

		Jwt jwt = (Jwt) auth.getPrincipal();

		String userIdentityProviderId = jwt.getSubject();
		
		if (userIdentityProviderId == null) {
			throw new UserNotFound("userIdentityId not present in token");
		}
		
		return userIdentityProviderId;
	}

	public BookSubscriptionsResponse getSubscriptions(String isbn) {
		return new BookSubscriptionsResponse(bookSubscriptionRepository.getSubscriptions(isbn).stream().map(this::toDto).toList());
	}
	
	private BookSubscriptionDto toDto(BookSubscriptionView view) {
		return new BookSubscriptionDto(view.id(), view.isbn(), view.userIdentityId(), view.status());
	}

}
