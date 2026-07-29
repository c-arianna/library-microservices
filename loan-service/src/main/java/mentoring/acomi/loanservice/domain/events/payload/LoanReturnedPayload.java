package mentoring.acomi.loanservice.domain.events.payload;

import java.time.LocalDate;

public record LoanReturnedPayload(String id, String isbn, String userId, LocalDate returnedAt) {}
