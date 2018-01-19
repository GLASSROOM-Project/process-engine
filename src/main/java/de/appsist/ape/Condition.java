package de.glassroom.gpe;

/**
 * Abstract model for a condition.
 * Conditions are used to control the process flow with gateways.
 * @author simon.schwantzer(at)im-c.de
 *
 */
public abstract class Condition {
    private final String type;
    private final String key;
    
    protected Condition(String type, String key) {
        this.type = type;
        this.key = key;
    }
    
    /**
     * Returns the type of the condition.
     * @return Condition type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Returns the key, i.e. variable with is addressed by the condition.
     * @return Identifier of a variable.
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Checks if the condition is fulfilled by an object.
     * @param object Object to run condition check on.
     * @return <code>true</code> if the condition is fulfilled, <code>false</code> otherwise.
     */
    public abstract boolean isFulfilledFor(Object object);
}