package mentoring.acomi.loanservice.infrastructure.persistence.entity;

import java.time.LocalDate;
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
import mentoring.acomi.loanservice.domain.model.LoanStatus;

@Entity
@Table(name = "loan_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanViewEntity {
	
	@Id
	private String id;
	
	private String isbn;
	private String userId;
	private LocalDate startDate;
	private LocalDate endDate;
	
	@Enumerated(EnumType.STRING)
	private LoanStatus status;
	
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
    
    public LoanViewEntity(String id, String isbn, String userId, LocalDate startDate, LocalDate endDate) {
    	this.id = id;
    	this.isbn = isbn;
    	this.userId = userId;
    	this.startDate = startDate;
    	this.endDate = endDate;
    	this.status = LoanStatus.PENDING;
    }

}
