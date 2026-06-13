package mentoring.acomi.userservice.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;

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
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	@PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserViewEntity(String id, String email, String name, String lastname, String userIdentityProviderId, UserStatus status, UserRole role) {
    	this.id = id;
    	this.email = email;
    	this.name = name;
    	this.lastname = lastname;
    	this.userIdentityProviderId = userIdentityProviderId;
    	this.status = status;
    	this.role = role;
    }

}
