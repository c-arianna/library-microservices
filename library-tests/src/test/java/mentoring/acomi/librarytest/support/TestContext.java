package mentoring.acomi.librarytest.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestContext {

	private Map<String, Object> context = new HashMap<>();

	public List<String> userProviderIdToDelete = new ArrayList<>();
	
	public void put(String key, Object value) {
		context.put(key, value);
	}

	public <T> T get(String key, Class<T> clazz) {
		return clazz.cast(context.get(key));
	}

	public Set<String> keys() {
		return context.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getTyped(String key) {
	    Object value = context.get(key);

	    if (value == null) {
	        throw new IllegalStateException("Missing key in context: ".formatted(key));
	    }

	    return (T) value;
	}

}
