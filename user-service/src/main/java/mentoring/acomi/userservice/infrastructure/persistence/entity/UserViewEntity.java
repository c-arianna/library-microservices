package mentoring.acomi.userservice.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@Entity
@Table(name = "user_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserViewEntity {
	
	@Id
	private String id;
	
	private String email;
	
	private String name;
	
	private String lastname;
	
	private String userIdentityProviderId;
	
	@Enumerated(EnumType.STRING)
	private UserStatus status;
	
	@Enumerated(EnumType.STRING)
	private UserRole role;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	private Instant updatedAt;
	    
    public UserViewEntity(String id, String email, String name, String lastname, String userIdentityProviderId, UserStatus status, UserRole role) {
    	this.id = id;
    	this.email = email;
    	this.name = name;
    	this.lastname = lastname;
    	this.userIdentityProviderId = userIdentityProviderId;
    	this.status = status;
    	this.role = role;
    }
    
    public void markCreated(Instant ts) {
		if (this.createdAt != null) {
			throw new IllegalStateException("createdAt already set");
		}
		this.createdAt = ts;
		this.updatedAt = ts;
	}

}
