package mentoring.acomi.userservice.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class User {
	
	private String id;
	private Email email; 
	private String name;
	private String lastname;
	private String userIdentityProviderId;
	private UserStatus status;
	private UserRole role;
	
	private User(String id, Email email, String name, String lastname, String identityProviderId, UserStatus status, UserRole role) {
        this.id = id;
       this.email = email;
       this.name = name;
       this.lastname = lastname;
       this.userIdentityProviderId = identityProviderId;
       this.status = status;
       this.role = role;
    }
	
	public static User create(String id, String email,String name, String lastname, String identityProviderId, UserRole role) {
        return new User(id, new Email(email), name, lastname, identityProviderId, UserStatus.ACTIVE, role);
    }

}
