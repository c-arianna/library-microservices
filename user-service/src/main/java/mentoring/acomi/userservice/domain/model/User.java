package mentoring.acomi.userservice.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

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
	private CardNumber cardNumber;
	private UserStatus status;
	private UserRole role;
	
	private User(String id, Email email, String name, String lastname, String identityProviderId, CardNumber cardNumber, UserStatus status, UserRole role) {
        this.id = id;
       this.email = email;
       this.name = name;
       this.lastname = lastname;
       this.userIdentityProviderId = identityProviderId;
       this.cardNumber = cardNumber;
       this.status = status;
       this.role = role;
    }
	
	public static User create(String id, String email,String name, String lastname, String identityProviderId, String cardNumber, UserRole role) {
        return new User(id, new Email(email), name, lastname, identityProviderId, new CardNumber(cardNumber),  UserStatus.ACTIVE, role);
    }

}
