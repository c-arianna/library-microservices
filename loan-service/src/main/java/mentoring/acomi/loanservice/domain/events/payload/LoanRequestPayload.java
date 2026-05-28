package mentoring.acomi.loanservice.domain.events.payload;

import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanRequestPayload(String id, String isbn, String userId, DateRange period, LoanStatus status) {

}
