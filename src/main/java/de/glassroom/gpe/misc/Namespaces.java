package de.glassroom.gpe.misc;

import org.jdom2.Namespace;

/**
 * Container for XML namespaces.
 *  
 * @author simon.schwantzer(at)im-c.de
 */
public interface Namespaces {
    public Namespace BPMN = Namespace.getNamespace("http://www.omg.org/spec/BPMN/20100524/MODEL");
    public Namespace METADATA = Namespace.getNamespace("glassroom:bpmn:metadata");
    public Namespace CONTENT = Namespace.getNamespace("glassroom:bpmn:content");
    public Namespace CONDITION = Namespace.getNamespace("glassroom:bpmn:condition");
    public Namespace CONTENTPACKAGE = Namespace.getNamespace("glassroom:content");
}
