package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserViewEntity;

@Component
public class UserViewJpaMapper {
	
	public UserViewEntity toEntity(UserView user) {
		return new UserViewEntity(user.id(), user.email(), user.identityProviderId(), user.status());
	}
	
	public UserView toDomain(UserViewEntity entity) {
		return new UserView(entity.getId(), entity.getEmail(), entity.getUserIdentityProviderId(), entity.getStatus());
	}

}
