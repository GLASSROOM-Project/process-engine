package de.glassroom.gpe;

/**
 * Condition checking if the variable equals a given value.
 * @author simon.schwantzer(at)im-c.de
 */
public class EqualsCondition extends Condition {
    public static final String TYPE = "equals";
    private final String value;

    /**
     * Creates a new equals condition.
     * @param key Variable identifier.
     * @param value Value to check equality against.
     */
    public EqualsCondition(String key, String value) {
        super(TYPE, key);
        this.value = value;
    }
    
    /**
     * Returns the value the variable is checked against.
     * @return Value.
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean isFulfilledFor(Object object) {
        return value.equals(object);
    }

}
