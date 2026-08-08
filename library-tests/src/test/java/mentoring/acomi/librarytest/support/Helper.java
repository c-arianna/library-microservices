package mentoring.acomi.librarytest.support;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.test.web.servlet.client.EntityExchangeResult;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

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
	
	public static String resolve(String value, TestContext context) {
		if (value.startsWith("${") && value.endsWith("}")) {
			String key = value.substring(2, value.length() - 1);
			return context.get(key, String.class);
		}
		return value;
	}
	
	public static void awaitAndAssert(Supplier<EntityExchangeResult<byte[]>> query, Consumer<DocumentContext> assertions, int timeoutMs, int intervalMs) {

		long start = System.currentTimeMillis();

		AssertionError lastError = null;

		while (System.currentTimeMillis() - start < timeoutMs) {

			try {

				var result = query.get();

				String body = new String(result.getResponseBody(), StandardCharsets.UTF_8);
				DocumentContext json = JsonPath.parse(body);

				assertions.accept(json);

				return;

			} catch (AssertionError e) {
			    lastError = e;
			} catch (PathNotFoundException e) {
			    lastError = new AssertionError(e);
			}

			try {
				Thread.sleep(intervalMs);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}

		if (lastError != null) {
			throw lastError;
		}

		throw new AssertionError("Condition not met within timeout");
	}

	
}
