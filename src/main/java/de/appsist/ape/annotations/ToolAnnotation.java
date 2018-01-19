package de.glassroom.gpe.annotations;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool annotation for process elements.
 * @author simon.schwantzer(at)im-c.de
 */
public class ToolAnnotation {
    private final String id;
    private final Map<String, String> params;
    
    public ToolAnnotation(String id) {
        this.id = id;
        this.params = new HashMap<>();
    }
    
    public void addParameter(String id, String value) {
        params.put(id, value);
    }
    
    public String getParameter(String id) {
        return params.get(id);
    }
    
    public Map<String, String> getParameteres() {
        return params;
    }
    
    public String getId() {
        return id;
    }
}
