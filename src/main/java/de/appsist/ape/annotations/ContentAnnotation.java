package de.glassroom.gpe.annotations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Content annotation for process elements.
 * A content annotation connects an step with the related content, the scene and didactical metadata. 
 * @author simon.schwantzer(at)im-c.de
 */
public class ContentAnnotation {
    private final Map<String, String> packages;
    private final Map<String, List<String>> warnings;
    private final List<ToolAnnotation> tools;
    private SceneAnnotation scene;
    private String lastKey;
    
    /**
     * Creates a content annotation.
     */
    public ContentAnnotation() {
        packages = new LinkedHashMap<>();
        warnings = new LinkedHashMap<>();
        tools = new ArrayList<>();
    }

    /**
     * Sets the content package for a specific language.
     * @param languageId ISO language code, e.g., "de_DE".
     * @param packageId Identifier of the content package to display for the step.
     * @return This for chaining.
     */
    public ContentAnnotation setContentPackage(String languageId, String packageId) {
        packages.put(languageId, packageId);
        lastKey = languageId;
        return this;
    }
    
    /**
     * Returns the content package for an arbitrary language.
     * @return Content package identifier or <code>null</code> of no content package is available.
     */
    public String getContentPackage() {
        return packages.get(lastKey);
    }
    
    /**
     * Returns the content package for a specific language.
     * @param languageId ISO language code, e.g., "de_DE".
     * @return Content package identifier or <code>null</code> of no content package is set for the given language.
     */
    public String getContentPackage(String languageId) {
        return packages.get(languageId);
    }
    
    /**
     * Returns the content packages for all languages.
     * @return Map with language code as key and content package identifiers as value. 
     */
    public Map<String, String> getContentPackages() {
        return packages;
    }
    
    /**
     * Adds a warning in a specific language.
     * @param languageId ISO language code, e.g., "de_DE".
     * @param warning Warning text.
     * @return This for chaining.
     */
    public ContentAnnotation addWarning(String languageId, String warning) {
        List<String> warningsForLanguage = warnings.get(languageId);
        if (warningsForLanguage == null) {
            warningsForLanguage = new ArrayList<String>();
            warnings.put(languageId, warningsForLanguage);
        }
        warningsForLanguage.add(warning);
        return this;
    }
    
    /**
     * Returns an ordered list of all warnings for a specific language.
     * @param languageId ISO language code, e.g., "de_DE".
     * @return List of warning texts. May be <code>null</code>.
     */
    public List<String> getWarnings(String languageId) {
        return warnings.get(languageId);
    }
    
    public Map<String, List<String>> getWarnings() {
        return warnings;
    }
        
    public ContentAnnotation addTool(ToolAnnotation tool) {
        tools.add(tool);
        return this;
    }

    public List<ToolAnnotation> getTools() {
        return tools;
    }
    
    /**
     * Sets the scene.
     * @param scene Scene to set.
     * @return This for chaining.
     */
    public ContentAnnotation setScene(SceneAnnotation scene) {
        this.scene = scene;
        return this;
    }
    
    /**
     * Returns the scene.
     * @return Interaction scene.
     */
    public SceneAnnotation getScene() {
        return scene;
    }
}
