package mentoring.acomi.librarytest.support;

import java.math.BigDecimal;

public class Helper {

	public static String normalize(String value) {

		if (value == null)
			return "";

		value = value.trim();

		if (value.equals("\"\""))
			return "";

		if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
			return value.substring(1, value.length() - 1);
		}

		return value;
	}
	
	public static ExpectedValue normalizeExpected(String raw) {

		if (raw == null) {
			return ExpectedValue.empty();
		}

		String value = raw.trim();

		if (value.equalsIgnoreCase("EMPTY")) {
			return ExpectedValue.empty();
		}

		if (value.equalsIgnoreCase("NULL")) {
			return ExpectedValue.nullValue();
		}

		if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
			value = value.substring(1, value.length() - 1);
			return ExpectedValue.ofString(value);
		}

		if (value.matches("[-+]?\\d+(\\.\\d+)?")) {
			return ExpectedValue.ofNumber(new BigDecimal(value));
		}

		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return ExpectedValue.ofBoolean(Boolean.parseBoolean(value));
		}

		return ExpectedValue.ofString(value);
	}
	
	public static String resolve(String value, TestContext world) {
		if (value.startsWith("${") && value.endsWith("}")) {
			String key = value.substring(2, value.length() - 1);
			return world.get(key, String.class);
		}
		return value;
	}
	
}
