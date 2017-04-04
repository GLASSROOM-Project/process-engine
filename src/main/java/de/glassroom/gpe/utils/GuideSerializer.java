package de.glassroom.gpe.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import de.glassroom.gpe.Branch;
import de.glassroom.gpe.Chapter;
import de.glassroom.gpe.Condition;
import de.glassroom.gpe.Decision;
import de.glassroom.gpe.EqualsCondition;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.GuideEnd;
import de.glassroom.gpe.GuideStart;
import de.glassroom.gpe.Node;
import de.glassroom.gpe.Step;
import de.glassroom.gpe.Tansition;
import de.glassroom.gpe.annotations.ContentAnnotation;
import de.glassroom.gpe.annotations.MetadataAnnotation;
import de.glassroom.gpe.annotations.SceneAnnotation;
import de.glassroom.gpe.annotations.ToolAnnotation;
import de.glassroom.gpe.misc.Namespaces;

/**
 * Serializes and deserializes a process in BPMN representation.
 * @author simon.schwantzer(at)im-c.de
 */
public final class GuideSerializer {
    private static SAXBuilder saxBuilder;
    
    static {
        saxBuilder = new SAXBuilder();
    }
    
    /**
     * Serializes the given assistance process as BPMN process.
     * @param process Process to serialize.
     * @param compact If set to <code>true</code>, the XML will be encoded without unnecessary whitespaces.
     * @return BPMN process XML String.
     */
    public static String writeAsBPMN(Guide process, boolean compact) {
        Element processElement = serializeAssistanceProcess(process);
        
        return XMLUtils.exportAsString(processElement, compact ? Format.getCompactFormat() : Format.getPrettyFormat());
    }
    
    /**
     * Parses an BPMN XML process string. 
     * @param processString BPMN process in XML representation.
     * @return Deserialized process.
     * @throws IllegalArgumentException Failed to parse XML string. 
     */
    public static Guide readFromBPMN(String processString) throws IllegalArgumentException {
        Element processElement;
        try {
            Document doc = saxBuilder.build(new StringReader(processString));
            processElement = doc.getRootElement();
            if (!"process".equals(processElement.getName())) {
                throw new IllegalArgumentException("Invalid process string: Route element does not match \"process\".");
            }
        } catch (JDOMException|IOException e) {
            throw new IllegalArgumentException("Failed to parse process string.", e);
        }
        
        return parseProcess(processElement);
        
    }
    
    private static Guide parseProcess(Element element) throws IllegalArgumentException {
        String id = XMLUtils.getRequiredAttribute(element, "id");
                
        Map<String, Node<?>> nodes = new LinkedHashMap<>();
        GuideStart processStart;
        
        for (Element child : element.getChildren()) {
            Node<?> node = null;
            switch (child.getName()) {
            case "startEvent":
                processStart = parseStartEvent(child);
                node = processStart;
                break;
            case "endEvent":
                node = parseEndEvent(child);
                break;
            case "userTask":
                node = parseUserTask(child);
                break;
            case "callActivity":
                node = parseCallActivity(child);
                break;
            case "exclusiveGateway":
                node = parseGateway(child);
                break;
            }
            if (node != null) {
                nodes.put(node.getId(), node);
            }
        }
        
        for (Element child : element.getChildren()) {
            if ("sequenceFlow".equals(child.getName())) {
                Tansition flow = parseFlow(child, nodes);
                flow.getSource().addOutgoing(flow);
                flow.getTarget().addIncoming(flow);
            }
        }
        
        Guide guide = new Guide(id, nodes);
        MetadataAnnotation metadata = extractMetadata(element);
        if (metadata != null) {
            guide.setMetadataWithoutUpdate(metadata);
        }
        
        return guide;
    }
    
    private static GuideStart parseStartEvent(Element element) throws IllegalArgumentException {
        GuideStart processStart = new GuideStart(XMLUtils.getRequiredAttribute(element, "id"));
        processStart.setName(element.getAttributeValue("name"));
        MetadataAnnotation metadata = extractMetadata(element);
        if (metadata != null) processStart.setMetadata(metadata);
        ContentAnnotation content = extractContent(element);
        if (content != null) processStart.setContent(content);
        
        return processStart;
    }
    
    private static GuideEnd parseEndEvent(Element element) throws IllegalArgumentException {
        GuideEnd processEnd = new GuideEnd(XMLUtils.getRequiredAttribute(element, "id"));
        processEnd.setName(element.getAttributeValue("name"));
        MetadataAnnotation metadata = extractMetadata(element);
        if (metadata != null) processEnd.setMetadata(metadata);
        ContentAnnotation content = extractContent(element);
        if (content != null) processEnd.setContent(content);
        return processEnd;
    }
    
    private static Step parseUserTask(Element element) throws IllegalArgumentException {
        Step supportedTask = new Step(XMLUtils.getRequiredAttribute(element, "id"));
        supportedTask.setName(element.getAttributeValue("name"));
        MetadataAnnotation metadata = extractMetadata(element);
        if (metadata != null) supportedTask.setMetadata(metadata);
        ContentAnnotation content = extractContent(element);
        if (content != null) supportedTask.setContent(content);
        return supportedTask;
    }
    
    private static Chapter parseCallActivity(Element element) throws IllegalArgumentException {
        Chapter subProcessCall = new Chapter(XMLUtils.getRequiredAttribute(element, "id"), XMLUtils.getRequiredAttribute(element, "calledElement"));
        subProcessCall.setName(element.getAttributeValue("name"));
        MetadataAnnotation metadata = extractMetadata(element);
        if (metadata != null) subProcessCall.setMetadata(metadata);
        ContentAnnotation content = extractContent(element);
        if (content != null) subProcessCall.setContent(content);
        return subProcessCall;
    }
    
    private static Branch parseGateway(Element element) throws IllegalArgumentException {
        Branch gateway = new Branch(XMLUtils.getRequiredAttribute(element, "id"));
        gateway.setName(element.getAttributeValue("name"));
        return gateway;
    }
    
    private static Tansition parseFlow(Element element, Map<String, Node<?>> nodes) throws IllegalArgumentException {
        String id = XMLUtils.getRequiredAttribute(element, "id");
        String sourceRef = element.getAttributeValue("sourceRef");
        String targetRef = element.getAttributeValue("targetRef");
        Node<?> source = nodes.get(sourceRef);
        Node<?> target = nodes.get(targetRef);
        
        Tansition flow;
        Element extensionElements = element.getChild("extensionElements", Namespaces.BPMN);
        if (extensionElements != null && XMLUtils.containsElement(extensionElements, "condition", Namespaces.CONDITION)) {
            MetadataAnnotation metadata = extractMetadata(element);
            if (metadata == null) {
                throw new IllegalArgumentException("Missig display for decision.");
            }
            Element conditionElement = XMLUtils.getRequiredElement(extensionElements, "condition", Namespaces.CONDITION);
            flow = new Decision(id, source, target, metadata.getTitles(), parseCondition(conditionElement));
        } else {
            flow = new Tansition(id, source, target);
        }
        
        return flow;
    }
    
    private static MetadataAnnotation extractMetadata(Element element) throws IllegalArgumentException {
        MetadataAnnotation metadata = null;
        Element extensionElements = element.getChild("extensionElements", Namespaces.BPMN);
        if (extensionElements != null) {
            Element metadataElement = extensionElements.getChild("metadata", Namespaces.METADATA);
            if (metadataElement != null) {
                metadata = new MetadataAnnotation();
                for (Element child : metadataElement.getChildren("title", Namespaces.METADATA)) {
                    metadata.setTitle(XMLUtils.getRequiredAttribute(child, "lang"), child.getText());
                }
                for (Element child : metadataElement.getChildren("description", Namespaces.METADATA)) {
                    metadata.setDescription(XMLUtils.getRequiredAttribute(child, "lang"), child.getText());
                }
                String lastUpdateString = metadataElement.getChildText("lastUpdate", Namespaces.METADATA);
                if (lastUpdateString != null) {
                    Date lastUpdate = ISODateTimeFormat.dateTime().parseDateTime(lastUpdateString).toDate();
                    metadata.setLastUpdate(lastUpdate);
                }
                Element vrSceneElement = metadataElement.getChild("vrScene", Namespaces.METADATA);
                if (vrSceneElement != null) {
                    String vrSceneId = XMLUtils.getRequiredAttribute(vrSceneElement, "id");
                    metadata.setVRScene(vrSceneId);
                    Element paramsElement = vrSceneElement.getChild("params", Namespaces.METADATA);
                    if (paramsElement != null) for (Element param : paramsElement.getChildren("param", Namespaces.METADATA)) {
                        String paramId = XMLUtils.getRequiredAttribute(param, "id");
                        String paramValue = XMLUtils.getRequiredAttribute(param, "value");
                        metadata.addVRSceneParameter(paramId, paramValue);
                    }
                }
            }
        }
        return metadata;
    }
    
    private static ContentAnnotation extractContent(Element element) throws IllegalArgumentException {
        ContentAnnotation content = null;
        Element extensionElements = element.getChild("extensionElements", Namespaces.BPMN);
        if (extensionElements != null) {
            Element contentElement = extensionElements.getChild("content", Namespaces.CONTENT);
            if (contentElement != null) {
                content = new ContentAnnotation();
                Element assistanceElement = contentElement.getChild("assistance", Namespaces.CONTENT);
                if (assistanceElement != null) for (Element packageElement : assistanceElement.getChildren("package", Namespaces.CONTENT)) {
                    content.setContentPackage(XMLUtils.getRequiredAttribute(packageElement, "lang"), packageElement.getText());
                }
                Element warningsElement = contentElement.getChild("warnings", Namespaces.CONTENT);
                if (warningsElement != null) for (Element warningElement : warningsElement.getChildren("warning", Namespaces.CONTENT)) {
                    content.addWarning(XMLUtils.getRequiredAttribute(warningElement, "lang"), warningElement.getText());
                }
                Element toolsElement = contentElement.getChild("tools", Namespaces.CONTENT);
                if (toolsElement != null) for (Element toolElement : toolsElement.getChildren("tool", Namespaces.CONTENT)) {
                    content.addTool(parseToolAnnotation(toolElement));
                }
                Element sceneElement =  contentElement.getChild("scene", Namespaces.CONTENT);
                if (sceneElement != null) {
                    content.setScene(parseSceneAnnotation(sceneElement));
                }
            }
        }
        return content;
    }
        
        private static ToolAnnotation parseToolAnnotation(Element element) throws IllegalArgumentException {
            Namespace ns = element.getNamespace();
            String id = XMLUtils.getRequiredAttribute(element, "id");
            
            ToolAnnotation annotation = new ToolAnnotation(id);
            Element paramsElement = element.getChild("params", ns);
            if (paramsElement != null) for (Element paramElement : paramsElement.getChildren("param", ns)) {
                annotation.addParameter(XMLUtils.getRequiredAttribute(paramElement, "id"), XMLUtils.getRequiredAttribute(paramElement, "value"));
            }
            
            return annotation;
        }
    
    private static SceneAnnotation parseSceneAnnotation(Element element) throws IllegalArgumentException {
        Namespace ns = element.getNamespace();
        Element nodeElement = XMLUtils.getRequiredElement(element, "node", ns);
        String nodeId = XMLUtils.getRequiredAttribute(nodeElement, "id");
        SceneAnnotation scene = new SceneAnnotation(nodeId);
        
        Element nodeParamsElement = nodeElement.getChild("params", ns);
        Map<String, String> nodeParameters = new LinkedHashMap<>();
        if (nodeParamsElement != null) for (Element paramElement : nodeParamsElement.getChildren("param", ns)) {
            String paramId = XMLUtils.getRequiredAttribute(paramElement, "id");
            String paramValue = XMLUtils.getRequiredAttribute(paramElement, "value");
            nodeParameters.put(paramId, paramValue);
        }
        scene.setNodeParameters(nodeParameters);
        
        Element methodElement = element.getChild("method", ns);
        if (methodElement != null) {
            String methodId = XMLUtils.getRequiredAttribute(methodElement, "id");
            Element methodParamsElement = methodElement.getChild("params", ns);
            Map<String, String> methodParameters = new LinkedHashMap<>();
            if (methodParamsElement != null) for (Element paramElement : methodParamsElement.getChildren("params", ns)) {
                String paramId = XMLUtils.getRequiredAttribute(paramElement, "id");
                String paramValue = XMLUtils.getRequiredAttribute(paramElement, "value");
                methodParameters.put(paramId, paramValue);
            }
            scene.setMethod(methodId, methodParameters);
        }
        
        return scene;
    }
    
    private static Condition parseCondition(Element element) throws IllegalArgumentException {
        Condition condition;
        
        String type = XMLUtils.getRequiredAttribute(element, "type");
        String key = XMLUtils.getRequiredAttribute(element, "key");
        switch (type) {
        case EqualsCondition.TYPE:
            String value = XMLUtils.getRequiredAttribute(element, "value");
            condition = new EqualsCondition(key, value);
            break;
        default:
            throw new IllegalArgumentException("Unsupported condition type: " + type);
        }
        
        return condition;
    }
    
    private static Element serializeAssistanceProcess(Guide process) throws IllegalArgumentException {
        Element element = new Element("process", Namespaces.BPMN);
        
        @SuppressWarnings("serial")
        Map<String, String> defaultAttributes = new HashMap<String, String>() {{
            put("isExecutable", "true");
        }};
        
        for (Entry<String, String> entry : defaultAttributes.entrySet()) element.setAttribute(entry.getKey(), entry.getValue());
        
        element.setAttribute("id", process.getId());
        
        MetadataAnnotation metadata = process.getMetadata();
        if (metadata != null) {
            Element extensionElements = new Element("extensionElements", Namespaces.BPMN);
            Element metadataElement = serializeMetadataAnnotation(metadata);
            extensionElements.addContent(metadataElement);
            element.addContent(extensionElements);
        }
        
        Element child = null;
        for (Node<?> node : process.getNodes()) {
            if (node instanceof GuideStart) {
                child = serializeProcessStart((GuideStart) node);
            } else if (node instanceof GuideEnd) {
                child = serializeProcessEnd((GuideEnd) node);
            } else if (node instanceof Step) {
                child = serializeSupportedTask((Step) node);
            } else if (node instanceof Chapter) {
                child = serializeSubProcessCall((Chapter) node);
            } else if (node instanceof Branch) {
                child = serializeGateway((Branch) node);
            }
            if (child == null) {
                throw new IllegalArgumentException("Unsupported node type: " + node);
            }
            element.addContent(child);
            for (Tansition flow : node.getOutgoing()) {
                Element flowElement = serializeFlow(flow);
                element.addContent(flowElement);
            }
        }
        
        return element;
    }
    
    private static Element serializeProcessStart(GuideStart processStart) {
        Element element = new Element("startEvent", Namespaces.BPMN);
        
        @SuppressWarnings("serial")
        Map<String, String> defaultAttributes = new HashMap<String, String>() {{
            put("isInterrupting", "true");
            put("parallelMultiple", "false");
        }};
        
        for (Entry<String, String> entry : defaultAttributes.entrySet()) element.setAttribute(entry.getKey(), entry.getValue());
        
        element.setAttribute("id", processStart.getId());
        if (processStart.getName() != null) element.setAttribute("name", processStart.getName());
        
        addExtensionElements(element, processStart);
        addIncomingAndOutgoing(element, processStart);
        
        return element;
    }
    
    private static void addExtensionElements(Element element, Node<?> node) {
        List<Element> extensions = new ArrayList<Element>();
        
        MetadataAnnotation metadata = node.getMetadata();
        if (metadata != null) {
            extensions.add(serializeMetadataAnnotation(metadata));
        }
        
        ContentAnnotation content = node.getContent();
        if (content != null) {
            extensions.add(serializeContentAnnotation(content));
        }
        
        if (extensions.size() > 0) {
            Element extensionElements = new Element("extensionElements", Namespaces.BPMN);
            for (Element extension : extensions) {
                extensionElements.addContent(extension);
            }
            element.addContent(extensionElements);
        }		
    }
    
    private static void addIncomingAndOutgoing(Element element, Node<?> node) {
        Namespace ns = Namespaces.BPMN;
        for (Tansition incoming : node.getIncoming()) {
            element.addContent(new Element("incoming", ns).setText(incoming.getId()));
        }
        for (Tansition outgoing : node.getOutgoing()) {
            element.addContent(new Element("outgoing", ns).setText(outgoing.getId()));
        }
    }
    
    private static Element serializeProcessEnd(GuideEnd processEnd) {
        Element element = new Element("endEvent", Namespaces.BPMN);

        element.setAttribute("id", processEnd.getId());
        if (processEnd.getName() != null) element.setAttribute("name", processEnd.getName());
        
        addExtensionElements(element, processEnd);
        addIncomingAndOutgoing(element, processEnd);
        
        return element;
        
    }
    
    private static Element serializeSupportedTask(Step task) {
        Element element = new Element("userTask", Namespaces.BPMN);
        
        @SuppressWarnings("serial")
        Map<String, String> defaultAttributes = new HashMap<String, String>() {{
            put("completionQuantity", "1");
            put("startQuantity", "1");
            put("isForCompensation", "false");
            put("implementation", "##unspecified");
        }};
        
        for (Entry<String, String> entry : defaultAttributes.entrySet()) element.setAttribute(entry.getKey(), entry.getValue());
        
        element.setAttribute("id", task.getId());
        if (task.getName() != null) element.setAttribute("name", task.getName());
        
        addExtensionElements(element, task);
        addIncomingAndOutgoing(element, task);
        
        return element;
    }

    private static Element serializeSubProcessCall(Chapter call) {
        Element element = new Element("callActivity", Namespaces.BPMN);
        
        @SuppressWarnings("serial")
        Map<String, String> defaultAttributes = new HashMap<String, String>() {{
            put("completionQuantity", "1");
            put("startQuantity", "1");
            put("isForCompensation", "false");
        }};
        
        for (Entry<String, String> entry : defaultAttributes.entrySet()) element.setAttribute(entry.getKey(), entry.getValue());
        
        element.setAttribute("id", call.getId());
        if (call.getName() != null) element.setAttribute("name", call.getName());
        element.setAttribute("calledElement", call.getCalledProcessId());
        
        addExtensionElements(element, call);
        addIncomingAndOutgoing(element, call);
        
        return element;
    }
    
    private static Element serializeGateway(Branch gateway) {
        Element element = new Element("exclusiveGateway", Namespaces.BPMN);
        
        @SuppressWarnings("serial")
        Map<String, String> defaultAttributes = new HashMap<String, String>() {{
            put("gatewayDirection", "##Diverging");
        }};
        
        for (Entry<String, String> entry : defaultAttributes.entrySet()) element.setAttribute(entry.getKey(), entry.getValue());
        
        element.setAttribute("id", gateway.getId());
        if (gateway.getName() != null) element.setAttribute("name", gateway.getName());
        
        addExtensionElements(element, gateway);
        addIncomingAndOutgoing(element, gateway);
        
        return element;
    }
    
    private static Element serializeFlow(Tansition flow) throws IllegalArgumentException {
        Element element = new Element("sequenceFlow", Namespaces.BPMN);
        element.setAttribute("id", flow.getId());
        element.setAttribute("sourceRef", flow.getSource().getId());
        element.setAttribute("targetRef", flow.getTarget().getId());
        if (flow instanceof Decision) {
            Element extensionElements = new Element("extensionElements", Namespaces.BPMN);

            Decision decision = (Decision) flow;
            
            Element metadataElement = new Element("metadata", Namespaces.METADATA);
            for (Entry<String, String> entry : decision.getDisplays().entrySet()) {
                Element titleElement = new Element("title", Namespaces.METADATA);
                titleElement.setAttribute("lang", entry.getKey());
                titleElement.setText(entry.getValue());
                metadataElement.addContent(titleElement);
            }
            extensionElements.addContent(metadataElement);
            
            Condition condition = decision.getCondition();
            Element conditionElement = new Element("condition", Namespaces.CONDITION);
            conditionElement.setAttribute("type", condition.getType());
            conditionElement.setAttribute("key", condition.getKey());
            switch (condition.getType()) {
            case EqualsCondition.TYPE:
                conditionElement.setAttribute("value", ((EqualsCondition) condition).getValue());
                break;
            default:
                throw new IllegalArgumentException("Unsupported condition type: " + condition);
            }
            extensionElements.addContent(conditionElement);
            
            element.addContent(extensionElements);
        }
        return element;
    }
    
    private static Element serializeMetadataAnnotation(MetadataAnnotation metadata) {
        Element element = new Element("metadata", Namespaces.METADATA);
        for (Entry<String, String> entry : metadata.getTitles().entrySet()) {
            Element titleElement = new Element("title", Namespaces.METADATA);
            titleElement.setAttribute("lang", entry.getKey());
            titleElement.setText(entry.getValue());
            element.addContent(titleElement);
        }
        for (Entry<String, String> entry : metadata.getDescriptions().entrySet()) {
            Element titleElement = new Element("description", Namespaces.METADATA);
            titleElement.setAttribute("lang", entry.getKey());
            titleElement.setText(entry.getValue());
            element.addContent(titleElement);
        }
        Date lastUpdate = metadata.getLastUpdate();
        if (lastUpdate != null) {
            Element lastUpdateElement = new Element("lastUpdate", Namespaces.METADATA);
            String lastUpdateString = ISODateTimeFormat.dateTime().print(new DateTime(lastUpdate));
            lastUpdateElement.setText(lastUpdateString);
            element.addContent(lastUpdateElement);
        }
        String vrSceneId = metadata.getVRScene();
        if (vrSceneId != null) {
            Element vrSceneElement = new Element("vrScene", Namespaces.METADATA);
            vrSceneElement.setAttribute("id", vrSceneId);
            if (!metadata.getVRSceneParameters().isEmpty()) {
                Element paramsElement = new Element("params", Namespaces.METADATA);
                for (Entry<String, String> entry : metadata.getVRSceneParameters().entrySet()) {
                    paramsElement.addContent(new Element("param", Namespaces.METADATA)
                            .setAttribute("id", entry.getKey())
                            .setAttribute("value", entry.getValue()));
                }
                vrSceneElement.addContent(paramsElement);
            }
            element.addContent(vrSceneElement);
        }
        return element;
    }

    private static Element serializeContentAnnotation(ContentAnnotation content) {
        Namespace ns = Namespaces.CONTENT;
        Element element = new Element("content", ns);
        
        if (!content.getContentPackages().isEmpty()) {
            Element assistanceElement = new Element("assistance", ns);
            for (Entry<String, String> entry : content.getContentPackages().entrySet()) {
                Element packageElement = new Element("package", ns);
                packageElement.setAttribute("lang", entry.getKey());
                packageElement.setText(entry.getValue());
                assistanceElement.addContent(packageElement);
            }
            element.addContent(assistanceElement);
        }
        
        if (!content.getWarnings().isEmpty()) {
            Element warningsElement = new Element("warnings", ns);
            for (Entry<String, List<String>> entry : content.getWarnings().entrySet()) {
                for (String warningText : entry.getValue()) {
                    Element warningElement = new Element("warning", ns);
                    warningElement.setAttribute("lang", entry.getKey());
                    warningElement.setText(warningText);
                    warningsElement.addContent(warningElement);
                }
            }
            element.addContent(warningsElement);
        }
                
        if (!content.getTools().isEmpty()) {
            Element toolsElement = new Element("tools", ns);
            for (ToolAnnotation tool : content.getTools()) {
                toolsElement.addContent(serializeToolAnnotation(tool));
            }
            element.addContent(toolsElement);
        }
        
        if (content.getScene() != null) {
            element.addContent(serializeSceneAnnotation(content.getScene()));
        }
        
        return element;
    }
        
    private static Element serializeToolAnnotation(ToolAnnotation tool) {
        Namespace ns = Namespaces.CONTENT;
        Element element = new Element("tool", ns);
        element.setAttribute("id", tool.getId());
        if (!tool.getParameteres().isEmpty()) {
            Element paramsElement = new Element("params", ns);
            for (Entry<String, String> param : tool.getParameteres().entrySet()) {
                Element paramElement = new Element("param", ns);
                paramElement.setAttribute("id", param.getKey());
                paramElement.setAttribute("value", param.getValue());
                paramsElement.addContent(paramElement);
            }
            element.addContent(paramsElement);
        }
        return element;
    }
    
    private static Element serializeSceneAnnotation(SceneAnnotation scene) {
        Namespace ns = Namespaces.CONTENT;
        Element element = new Element("scene", ns);
        
        Element nodeElement = new Element("node", ns);
        nodeElement.setAttribute("id", scene.getNodeId());
        if (!scene.getNodeParameters().isEmpty()) {
            Element paramsElement = new Element("params", ns);
            for (Entry<String, String> param : scene.getNodeParameters().entrySet()) {
                Element paramElement = new Element("param", ns);
                paramElement.setAttribute("id", param.getKey());
                paramElement.setAttribute("value", param.getValue());
                paramsElement.addContent(paramElement);
            }
            nodeElement.addContent(paramsElement);
        }
        element.addContent(nodeElement);
        
        String methodId = scene.getMethodId();
        if (methodId != null) {
            Element methodElement = new Element("method", ns);
            methodElement.setAttribute("id", methodId);
            if (!scene.getMethodParameters().isEmpty()) {
                Element paramsElement = new Element("params", ns);
                for (Entry<String, String> param : scene.getMethodParameters().entrySet()) {
                    Element paramElement = new Element("param", ns);
                    paramElement.setAttribute("id", param.getKey());
                    paramElement.setAttribute("value", param.getValue());
                    paramsElement.addContent(paramElement);
                }
                methodElement.addContent(paramsElement);
            }
            element.addContent(methodElement);
        }
        
        return element;
    }
}

