package de.glassroom.gpe;


/**
 * Model for a flow between two steps of a process.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class Tansition {
    
    private final String id;
    private final Node<?> source;
    private final Node<?> target;

    /**
     * Creates a new flow between two process elements.
     * @param source Element from which the flow will start.
     * @param target Target where the flow will end.
     */
    public Tansition(Node<?> source, Node<?> target) {
        this("flow-" + source.getId() + "_" + target.getId(), source, target);
    }
    
    public Tansition(String id, Node<?> source, Node<?> target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    /**
     * Returns the source element of the flow.
     * @return Element from which the flow starts.
     */
    public Node<?> getSource() {
        return source;
    }
    
    /**
     * Returns the target element of the flow.
     * @return Element where the flow ends.
     */
    public Node<?> getTarget() {
        return target;
    }

    public String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
            .append("(")
            .append(source.getId())
            .append("->")
            .append(target.getId())
            .append(")").toString();
    }
}
