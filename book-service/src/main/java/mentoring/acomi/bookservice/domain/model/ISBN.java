package mentoring.acomi.bookservice.domain.model;

import java.util.regex.Pattern;

import mentoring.acomi.bookservice.domain.errors.InvalidIsbn;

public abstract class ISBN {

	private String value;
	private Pattern format;
	private Pattern ruleRegex;
	private String ruleFormat;

	public ISBN(String rawIsbn, Pattern format, Pattern ruleRegex, String ruleFormat) {

		this.value = normalize(rawIsbn);
		this.format = format;
		this.ruleRegex = ruleRegex;
		this.ruleFormat = ruleFormat;

		if (!this.format.matcher(this.value).matches() || !this.checksum(this.value)) {
			throw InvalidIsbn.invalidFormat(this.value);
		}

	}

	public String getValue() {
		return value;
	}
	
	public String formatted() {
		return this.ruleRegex.matcher(this.value).replaceAll(this.ruleFormat);
	}
	
	private static String normalize(String input) {
		return input.toUpperCase().replaceAll("[^0-9X]", "");
	}

	abstract boolean checksum(String value);

	public static ISBN of(String value) {
		
		if (ISBN10.check(value)) {
			return new ISBN10(value);
		}
		
		if (ISBN13.check(value)) {
			return new ISBN13(value);
		}

		 throw InvalidIsbn.invalidFormat(value);
	}

}
