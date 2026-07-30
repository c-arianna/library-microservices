package mentoring.acomi.loanservice.application.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OverdueStatisticDto(String userId, String cardNumber, int overdueLoansCount, Long activeOverdueLoansCount, long totalDaysOverdue, 
		LocalDate lastOverdueDate) {
	
	@JsonProperty
	public RiskLevel riskLevel() {

	    if (activeOverdueLoansCount >= 2 ||
	        overdueLoansCount >= 10) {
	        return RiskLevel.HIGH;
	    }

	    if (activeOverdueLoansCount >= 1 ||
	        overdueLoansCount >= 5) {
	        return RiskLevel.MEDIUM;
	    }

	    return RiskLevel.LOW;
	}
	
}
