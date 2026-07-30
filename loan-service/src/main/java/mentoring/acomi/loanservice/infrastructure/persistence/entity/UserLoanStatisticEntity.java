package mentoring.acomi.loanservice.infrastructure.persistence.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_loan_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoanStatisticEntity {
	
	@Id
	private String userId;
	
	private int overdueLoansCount;
	
	private long totalDaysOverdue;
	
	private LocalDate lastOverdueDate;

	public UserLoanStatisticEntity(String userId, int overdueLoansCount, long totalDaysOverdue, LocalDate lastOverdueDate) {
		this.userId = userId;
		this.overdueLoansCount = overdueLoansCount;
		this.totalDaysOverdue = totalDaysOverdue;
		this.lastOverdueDate = lastOverdueDate;
	}
	
}
