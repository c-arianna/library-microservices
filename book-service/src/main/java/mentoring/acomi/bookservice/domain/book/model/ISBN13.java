package mentoring.acomi.bookservice.domain.book.model;

import java.util.regex.Pattern;

public class ISBN13 extends ISBN {

	private static final Pattern format = Pattern.compile("^\\d{13}$");
	private static final Pattern ruleRegex = Pattern.compile("^(\\d)(\\d{4})(\\d{4})([\\dX])$");
	private static final String ruleFormat = "$1-$2-$3-$4";

	public ISBN13(String rawIsbn) {
		super(rawIsbn, format, ruleRegex, ruleFormat);
	}

	@Override
	boolean checksum(String value) {
		int sum = 0;
		for (int i = 0; i < 12; i++) {
			sum += (i % 2 != 0 ? 3 : 1) * (value.charAt(i) - 48);
		}

		return (10 - (sum % 10)) % 10 == value.charAt(12) - 48;
	}

	public static boolean check(String value) {
		return value.replaceAll("\\D", "").length() == 13;
	}

}
