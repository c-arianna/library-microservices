package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.view.BookSubscriptionView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookSubscriptionEntity;

@Component
public class BookSubscriptionJpaMapper {

	public BookSubscriptionEntity toEntity(BookSubscriptionView view) {
		return new BookSubscriptionEntity(view.isbn(), view.userIdentityId(), view.phoneNumber(), view.status(), view.type(), view.createdAt());
	}
	
	public BookSubscriptionView toDomain(BookSubscriptionEntity entity) {
		return new  BookSubscriptionView(entity.getId(), entity.getIsbn(), entity.getUserIdentityId(), entity.getPhoneNumber(), entity.getStatus(),
				entity.getSubscriptionType(), entity.getCreatedAt(), entity.getNotifiedAt());
	}
}
