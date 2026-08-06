package mentoring.acomi.bookservice.domain.book.model;

import mentoring.acomi.bookservice.domain.errors.InvalidTitle;

public class Title extends ValueObject<String> {

	public Title(String value)  {
		super(value);
	}

	@Override
	public String validate(String title) {

		if (title == null || title.trim().length() == 0) {
			throw InvalidTitle.empty();
		}

		return title.trim();
	}

}
