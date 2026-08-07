package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.view.UserView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.UserViewEntity;

@Component
public class UserViewJpaMapper {

	public UserViewEntity toEntity(UserView user) {
		return new UserViewEntity(user.id(), user.email(), user.name(), user.lastname(), user.cardNumber(), 
				user.userIdentityProviderId(), user.status());
	}
	
	public UserView toDomain(UserViewEntity entity) {
		return new UserView(entity.getId(), entity.getEmail(), entity.getName(), entity.getLastname(), entity.getUserIdentityProviderId(),
				entity.getCardNumber(), entity.getStatus());
	}
}
