package mentoring.acomi.librarytest.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestContext {

	private Map<String, Object> context = new HashMap<>();

	public void put(String key, Object value) {
		context.put(key, value);
	}

	public <T> T get(String key, Class<T> clazz) {
		return clazz.cast(context.get(key));
	}

	public Set<String> keys() {
		return context.keySet();
	}

}
