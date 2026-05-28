package mentoring.acomi.bookservice.domain.model;

import mentoring.acomi.bookservice.domain.errors.InvalidAuthor;

public class Author extends ValueObject<String> {

	public Author(String value)  {
		super(value);
	}

	@Override
	public String validate(String author) {

		if (author == null || author.trim().length() == 0) {
			throw InvalidAuthor.empty();
		}

		return author.trim();

	}

}
