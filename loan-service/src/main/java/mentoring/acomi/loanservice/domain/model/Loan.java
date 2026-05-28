package mentoring.acomi.loanservice.domain.model;

import java.time.LocalDate;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public class Loan {
	
	private String id;
	private ISBN isbn;
	private String userId;
	private LoanStatus status;
	private DateRange period;
	
	private Loan(String id, ISBN isbn, String userId, LoanStatus status, DateRange period) {
        this.id = id;
        this.isbn = isbn;
        this.userId = userId;
        this.status = status;
        this.period = period;
    }
	
	public static Loan create(String id, String isbn, String userId, LocalDate start, LocalDate end) {
		DateRange period = new DateRange(start, end);
        return new Loan(id, ISBN.of(isbn), userId, LoanStatus.PENDING, period);
    }
	
	public String getId() {
		return id;
	}
	
	public String getIsbn() {
    	return isbn!= null ? isbn.getValue() : "";
    }
	
	public String getUserId() {
		return userId;
	}
	
	public LoanStatus getStatus() {
		return status;
	}
	
	public DateRange getPeriod() {
		return period;
	}
	

}
