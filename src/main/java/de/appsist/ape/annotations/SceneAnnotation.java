package de.glassroom.gpe.annotations;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Annotation for information about the VR scene. 
 * @author simon.schwantzer(at)im-c.de
 */
public class SceneAnnotation {
    private final String nodeId;
    private String methodId;
    private Map<String, String> nodeParameters;
    private Map<String, String> methodParameters;
    
    /**
     * Creates a scene annotation.
     * @param nodeId Identifier for the interactive node in the scene.
     */
    public SceneAnnotation(String nodeId) {
        this.nodeId = nodeId;
        nodeParameters = new LinkedHashMap<>();
        methodParameters = new LinkedHashMap<>();
    }
    
    /**
     * Returns the VR method to be applied.
     * @return Indentifier of a VR method.
     */
    public String getMethodId() {
        return methodId;
    }
    
    /**
     * Returns the parameters for the VR method.
     * @return Map of key and values for method-specific parameters. May be empty.
     */
    public Map<String, String> getMethodParameters() {
        return methodParameters;
    }
    
    /**
     * Sets the VR method to be applied.
     * @param methodId Identifier of the method.
     * @param params Method parameters. May be empty.
     */
    public void setMethod(String methodId, Map<String, String> params) {
        this.methodId = methodId;
        this.methodParameters = params;
    }
    
    /**
     * Returns the interactive node in the scene.
     * @return Node identifier.
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * Returns the parameters for the VR node.
     * @return Map of key and values for node-specific parameters. May be empty.
     */
    public Map<String, String> getNodeParameters() {
        return nodeParameters;
    }
    
    /**
     * Sets the parameters for the VR node.
     * @param params Map of key and values for node-specific parameters. May be empty.
     */
    public void setNodeParameters(Map<String, String> params) {
        nodeParameters = params;
    }

}
