package de.glassroom.gpe;


/**
 * Model for a process start.
 * This should be automatically created for each process.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class GuideStart extends Node<GuideStart> {
    /**
     * Creates a new start element for a assistance process.
     */
    public GuideStart() {
        super("startEvent");
        setName("Start Event");
    }
    
    public GuideStart(String id) {
        super("startEvent", id);
        setName("Start Event");
    }

    @Override protected GuideStart getThis() { return this; }
    
    @Override
    public GuideStart addPrevious(Node<? extends Node<?>> source) {
        throw new UnsupportedOperationException("A start event may not have any predecessors.");
    }
}
