package de.glassroom.gpe;

import de.glassroom.gpe.annotations.ContentAnnotation;

/**
 * Model for a supported task.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class Step extends Node<Step> {
    /**
     * Creates a new supported task.
     */
    public Step() {
        super("userTask");
    }
    
    /**
     * Creates a new supported task with a given identifier.
     * @param id Identifier for the process element.
     */
    public Step(String id) {
        super("userTask", id);
    }
    
    @Override protected Step getThis() { return this; }
    
    /**
     * Sets a content package describing the assistance step.
     * @param languageId ISO language code, e.g., "de_DE".
     * @param packageId Identifier for an external content package.
     */
    public void setContentPackage(String languageId, String packageId) {
        ContentAnnotation content = super.getContent();
        if (content == null) {
            content = new ContentAnnotation();
            super.setContent(content);
        }
        
        content.setContentPackage(languageId, packageId);
    }
}
