package mentoring.acomi.userservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;

@Component
public class UserViewJpaMapper {
	
	public UserViewEntity toEntity(UserView user) {
		return new UserViewEntity(user.id(), user.email(), user.name(), user.lastname(), user.userIdentityProviderId(), 
				user.cardNumber(), user.status(), user.role());
	}
	
	public UserView toDomain(UserViewEntity entity) {
		return new UserView(entity.getId(), entity.getEmail(), entity.getName(), entity.getLastname(), entity.getUserIdentityProviderId(), 
				entity.getCardNumber(), entity.getStatus(), entity.getRole());
	}

}
