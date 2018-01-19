package de.glassroom.gpe;


/**
 * Model for a process end.
 * For each process an end element is automatically created, but additional end events are possible.  
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class GuideEnd extends Node<GuideEnd> {
    /**
     * Creates a new end element for an assistance process.
     */
    public GuideEnd() {
        super("endEvent");
        setName("End Event");
    }
    
    /**
     * Creates a new end element for an assistance process.
     * @param id Identifier for the element.
     */
    public GuideEnd(String id) {
        super("endEvent", id);
        setName("End Event");
    }

    @Override
    public GuideEnd addNext(Node<? extends Node<?>> target) {
        throw new UnsupportedOperationException("The end event cannot have successors.");
    }

    @Override protected GuideEnd getThis() { return this; }
}
