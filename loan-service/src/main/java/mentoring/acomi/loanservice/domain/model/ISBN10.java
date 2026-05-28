package mentoring.acomi.loanservice.domain.model;

import java.util.regex.Pattern;

public class ISBN10 extends ISBN {

	private static final Pattern format = Pattern.compile("^\\d{9}[\\dX]$");
	private static final Pattern ruleRegex = Pattern.compile("^(\\d)(\\d{4})(\\d{4})([\\dX])$");
	private static final String ruleFormat = "$1-$2-$3-$4";

	public ISBN10(String rawIsbn) {
		super(rawIsbn, format, ruleRegex, ruleFormat);
	}

	@Override
	boolean checksum(String value) {
		int sum = 0;
		for (int i = 0; i < 9; i++) {
			sum += (10 - i) * (value.charAt(i) - 48);
	    }
	    int last = value.charAt(9) == 'X' ? 10 : value.charAt(9) - 48;
		return (sum + last) % 11 == 0;
	}
	
	public static boolean check(String value) {
		return value.replaceAll("[^0-9X]", "").length() == 10;
	}

}
