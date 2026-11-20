package mentoring.acomi.assistantservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.UserViewEntity;

@Component
public class UserViewJpaMapper implements ViewMapper<UserView, UserViewEntity>{

	public UserViewEntity toEntity(UserView user) {
		return new UserViewEntity(user.userId(), user.email(), user.userIdentityProviderId(), user.status());
	}
	
	public UserView toView(UserViewEntity entity) {
		
		if(entity == null) {
			return null;
		}
		
		return new UserView(entity.getId(), entity.getEmail(), entity.getUserIdentityProviderId(), entity.getStatus());	
	}

}