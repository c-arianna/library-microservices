package mentoring.acomi.loanservice.domain.model;

import java.time.LocalDate;

import lombok.Getter;
import mentoring.acomi.loanservice.domain.errors.InvalidDateRange;

@Getter
public final class DateRange {

	private final LocalDate start;
	private final LocalDate end;

	public DateRange(LocalDate start, LocalDate end) {

		if (start == null) {
			start = LocalDate.now();
		}

		if (end == null) {
			end = start.plusDays(30);
		}

		if (end.isBefore(start)) {
			throw new InvalidDateRange("End date before start date");
		}

		this.start = start;
		this.end = end;
	}

}
