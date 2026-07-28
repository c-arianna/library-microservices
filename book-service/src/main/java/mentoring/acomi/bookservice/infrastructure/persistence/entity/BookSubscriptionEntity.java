package mentoring.acomi.bookservice.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.SubscriptionType;

@Entity
@Table(name = "book_subscriptions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String isbn;

	private String userIdentityId;

	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	private BookSubscriptionStatus status;

	@Enumerated(EnumType.STRING)
	private SubscriptionType subscriptionType;
	
	private Instant notifiedAt;

	private Instant createdAt;
	
	public BookSubscriptionEntity(String isbn, String userIdentityId, String phoneNumber, BookSubscriptionStatus status, 
			SubscriptionType subscriptionType, Instant createdAt) {
		this.isbn = isbn;
		this.userIdentityId = userIdentityId;
		this.phoneNumber = phoneNumber;
		this.status = status;
		this.subscriptionType = subscriptionType;
		this.createdAt = createdAt;
	}

}
