package mentoring.acomi.librarytest.support;

import java.math.BigDecimal;

public class ExpectedValue {
    
	enum Type { STRING, NUMBER, BOOLEAN, NULL, EMPTY }

    private final Type type;
    private final Object value;

    private ExpectedValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static ExpectedValue ofString(String value) {
        return new ExpectedValue(Type.STRING, value);
    }

    public static ExpectedValue ofNumber(BigDecimal value) {
        return new ExpectedValue(Type.NUMBER, value);
    }

    public static ExpectedValue ofBoolean(boolean value) {
        return new ExpectedValue(Type.BOOLEAN, value);
    }

    public static ExpectedValue nullValue() {
        return new ExpectedValue(Type.NULL, null);
    }

    public static ExpectedValue empty() {
        return new ExpectedValue(Type.EMPTY, "");
    }

    public boolean matches(Object actual) {
        
    	switch (type) {

            case NULL:
                return actual == null;

            case EMPTY:
                return actual == null || String.valueOf(actual).isEmpty();

            case STRING:
                return actual != null && String.valueOf(actual).equals(value);
                
            case BOOLEAN:
                return actual instanceof Boolean b ? b.equals(value) : 
                	String.valueOf(actual).equalsIgnoreCase(value.toString());

            case NUMBER:
                if (actual instanceof Number n) {
                    BigDecimal actualNum = new BigDecimal(n.toString());
                    return actualNum.compareTo((BigDecimal) value) == 0;
                }
                return false;
        
    	}

        return false;
    }
}
