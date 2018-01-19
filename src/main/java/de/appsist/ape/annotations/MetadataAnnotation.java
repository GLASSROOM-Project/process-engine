package de.glassroom.gpe.annotations;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metadata annotation for process elements.
 * @author simon.schwantzer(at)im-c.de
 */
public class MetadataAnnotation {
    
    private final Map<String, String> titles;
    private final Map<String, String> descriptions;
    private Date lastUpdate;
    private String lastKey;
    private String vrSceneId;
    private final Map<String, String> vrSceneParameters;
    
    /**
     * Creates a metadata annotation.
     */
    public MetadataAnnotation() {
        this.titles = new LinkedHashMap<>();
        this.descriptions = new LinkedHashMap<>();
        this.vrSceneParameters = new LinkedHashMap<>();
    }

    /**
     * Sets the title in a specific language.
     * @param lang ISO language code, e.g. "de_DE".
     * @param title Title to display.
     * @return Annotation object for chaining.
     */
    public MetadataAnnotation setTitle(String lang, String title) {
        titles.put(lang, title);
        lastKey = lang;
        return this;
    }
    
    /**
     * Returns the title in an arbitrary language. This should only be used if only one language is supported.
     * @return Title or <code>null</code> if no title is available.
     */
    public String getTitle() {
        return titles.get(lastKey);
    }
    
    /**
     * Returns the title in a specific language.
     * @param lang ISO language code, e.g. "de_DE".
     * @return Title or <code>null</code> if no title is set for the language.
     */
    public String getTitle(String lang) {
        return titles.get(lang);
    }
    
    /**
     * Returns the title for all languages.
     * @return Map with language code as key and title as value.
     */
    public Map<String, String> getTitles() {
        return titles;
    }
    
    /**
     * Sets the description in a specific language.
     * @param lang ISO language code, e.g., "de_DE".
     * @param description Description for the process or step.
     * @return Annotation object for chaining.
     */
    public MetadataAnnotation setDescription(String lang, String description) {
        descriptions.put(lang, description);
        lastKey = lang;
        return this;
    }
    
    /**
     * Returns the description in an arbitrary language.
     * @return Description or <code>null</code> if no description is available.
     */
    public String getDescription() {
        return descriptions.get(lastKey);
    }
    
    /**
     * Returns the description in a specific language.
     * @param lang ISO language code, e.h., "de_DE".
     * @return Description or <code>null</code> if no title is set for the language.
     */
    public String getDescription(String lang) {
        return descriptions.get(lang); 
    }
    
    /**
     * Returns the descriptions for all languages.
     * @return Map with language code as key and description as value.
     */
    public Map<String, String> getDescriptions() {
        return descriptions;
    }
    
    /**
     * Returns the date time of the last update. 
     * @return Date time or <code>null</code> if not set.
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    /**
     * Sets the date time of the last update.
     * @param date Date time to set. May be <code>null</code>.
     */
    public void setLastUpdate(Date date) {
        this.lastUpdate = date;
    }
    
    /**
     * Returns the VR scene connected to this node.
     * @return Identifier for the VR scene.
     */
    public String getVRScene() {
        return vrSceneId;
    }
    
    /**
     * Connects the node with a VR scene.
     * @param sceneId Identifier for the VR scene.
     */
    public void setVRScene(String sceneId) {
        this.vrSceneId = sceneId;
    }
    
    /**
     * Removes the link to a VR scene.
     * Nohting will happen if no VR scene is linked.
     */
    public void removeVRScene() {
        this.vrSceneId = null;
    }
    
    /**
     * Returs all VR scene parameters.
     * @return Map with parameter keys and values.
     */
    public Map<String, String> getVRSceneParameters() {
        return vrSceneParameters;
    }
    
    /**
     * Returns a specific VR scene parameter.
     * @param key Key of the parameter to return.
     * @return Value of the parameter with the given key or <code>null</code> if no such key exists.
     */
    public String getVRSceneParameter(String key) {
        return vrSceneParameters.get(key);
    }
    
    /**
     * Adds a VR scene parameter.
     * @param key Key of the parameter.
     * @param value Value of the parameter.
     */
    public void addVRSceneParameter(String key, String value) {
        vrSceneParameters.put(key, value);
    }
    
    /**
     * Removes a VR scene parameter.
     * If no value exists for the given ID, no operation will be performed.
     * @param key Key of the parameter to remove.
     */
    public void removeVRSceneParameter(String key) {
        vrSceneParameters.remove(key);
    }
    
    /**
     * Removes all VR scene parameters.
     */
    public void clearVRSceneParameters() {
        vrSceneParameters.clear();
    }
}
