package mentoring.acomi.userservice.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class User {
	
	private String id;
	private Email email; 
	private String name;
	private String lastname;
	private Password password;
	private UserStatus status;
	private UserRole role;
	
	private User(String id, Email email, String name, String lastname, Password password, UserStatus status, UserRole role) {
        this.id = id;
       this.email = email;
       this.name = name;
       this.lastname = lastname;
       this.password = password;
       this.status = status;
       this.role = role;
    }
	
	public static User create(String id, String email,String name, String lastname, Password password, UserRole role) {
        return new User(id, new Email(email), name, lastname, password, UserStatus.ACTIVE, role);
    }

}
