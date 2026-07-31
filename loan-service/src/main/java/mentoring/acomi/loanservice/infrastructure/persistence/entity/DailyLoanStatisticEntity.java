package mentoring.acomi.loanservice.infrastructure.persistence.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_loan_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyLoanStatisticEntity {
	
	@Id
	private LocalDate statisticsDate;
	
	private int loansCreated;
	
	private int loansConfirmed;
	
	private int loansCanceled;
	
	private int loansReturned;
	
	public DailyLoanStatisticEntity(LocalDate statisticsDate, int loansCreated, int loansConfirmed, int loansCanceled, int loansReturned) {
		this.statisticsDate = statisticsDate;
		this.loansCreated = loansCreated;
		this.loansConfirmed = loansConfirmed;
		this.loansCanceled = loansCanceled;
		this.loansReturned = loansReturned;
	}

}
