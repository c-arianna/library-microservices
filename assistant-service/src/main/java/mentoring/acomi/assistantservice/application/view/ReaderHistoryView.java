package mentoring.acomi.assistantservice.application.view;

import java.time.LocalDate;

import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

public record ReaderHistoryView(String loanId, String userId, String isbn, LocalDate borrowedAt, LocalDate returnedAt,
		LoanStatus loanStatus) implements ProjectionView<String> {

	@Override
	public String id() {
		return loanId();
	}

}
