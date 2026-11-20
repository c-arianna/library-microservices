package mentoring.acomi.assistantservice.infrastructure.persistence.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

@Entity
@Table(name = "reader_history_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderHistoryViewEntity implements ProjectionEntity {
	
	@Id
	private String loanId;
	
	private String userId;
	
	private String isbn;
	
	private LocalDate borrowedAt;
	
	private LocalDate returnedAt;
	
	@Enumerated(EnumType.STRING)
	private LoanStatus loanStatus;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	private Instant updatedAt;
	
	public ReaderHistoryViewEntity(String loanId, String userId, String isbn, LocalDate borrowedAt, LocalDate returnedAt, 
			LoanStatus loanStatus) {
    	this.loanId = loanId;
    	this.userId = userId;
    	this.isbn = isbn;
    	this.borrowedAt = borrowedAt;
    	this.returnedAt = returnedAt;
    	this.loanStatus = loanStatus;
    }
    
	@Override
    public void markUpdated(Instant ts) {
		if (this.createdAt == null) {
			this.createdAt = ts;
		}
		this.updatedAt = ts;
	}
}
