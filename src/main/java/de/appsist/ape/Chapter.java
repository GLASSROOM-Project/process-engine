package de.glassroom.gpe;


/**
 * A call activity, i.e. a node referencing a sub process to instantiate.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class Chapter extends Node<Chapter> {
    
    private String calledProcess;
    /**
     * Creates a sub process call of the given process.
     * @param calledProcessId Identifier of the assistance process to instantiate and proceed with.
     */
    public Chapter(String calledProcessId) {
        super("callActivity");
        this.calledProcess = calledProcessId;
    }
    
    public Chapter(String id, String calledProcessId) {
        super("callActivity", id);
        this.calledProcess = calledProcessId;
    }

    @Override protected Chapter getThis() { return this; }

    /**
     * Returns the called process.
     * @return Identifier of the process called during this step.
     */
    public String getCalledProcessId() {
        return calledProcess;
    }
    
    /**
     * Sets the called process.
     * @param calledProcessId Identifier of the process called during this step.
     */
    public void setCalledProcessId(String calledProcessId) {
        this.calledProcess = calledProcessId;
    }

}
