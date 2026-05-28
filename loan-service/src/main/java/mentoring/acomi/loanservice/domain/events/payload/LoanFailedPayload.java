package mentoring.acomi.loanservice.domain.events.payload;

import mentoring.acomi.loanservice.domain.events.LoanFailedReason;

public record LoanFailedPayload(String id, LoanFailedReason reason) {

}
