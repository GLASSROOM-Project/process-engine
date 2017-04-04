package de.glassroom.gpe.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;

import de.glassroom.gpe.content.ContentDescriptor;
import de.glassroom.gpe.content.Hint;
import de.glassroom.gpe.content.Warning;
import de.glassroom.gpe.misc.Namespaces;

/**
 * Serializer and deserializer for contest descriptors. 
 * @author simon.schwantzer(at)im-c.de
 */
public final class ContentSerializer {
    private static SAXBuilder saxBuilder;
    
    static {
        saxBuilder = new SAXBuilder();
    }
    
    /**
     * Tries to deserializes a content descriptor.
     * @param content Serialized content descriptor. 
     * @return Deserialized content descriptor.
     * @throws IllegalArgumentException Failed to de-serialize the given string.
     */
    public static ContentDescriptor readFromString(String content) throws IllegalArgumentException {
        String trimmedContent = content.trim();
        if (trimmedContent.startsWith("<")) {
            return readFromXML(trimmedContent);
        } else if (trimmedContent.startsWith("{")) {
            return readFromJSON(trimmedContent);
        } else {
            throw new IllegalArgumentException("Unknown content format.");
        }
    }
    
    /**
     * Deserializes a content descriptor from a XML string.
     * @param xmlString XML string encoding a content descriptor. 
     * @return Content descriptor.
     * @throws IllegalArgumentException The given XML string is no valid encoding of a content descriptor.
     */
    public static ContentDescriptor readFromXML(String xmlString) throws IllegalArgumentException{
        Element contentElement;
        ContentDescriptor descriptor;
        try {
            Document doc = saxBuilder.build(new StringReader(xmlString));
            contentElement = doc.getRootElement();
            if (!"content".equals(contentElement.getName())) {
                throw new IllegalArgumentException("Invalid content manifest: Route element does not match \"content\".");
            }
        } catch (JDOMException|IOException e) {
            throw new IllegalArgumentException("Failed to parse content descriptor.", e);
        }
        Namespace ns = contentElement.getNamespace();
        
        String id = contentElement.getAttributeValue("id");
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required [id].");
        }
        String languageId = contentElement.getAttributeValue("lang");
        if (languageId == null || languageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing language identifier [lang].");
        }
        
        descriptor = new ContentDescriptor(id, languageId);
        
        descriptor.setVersion(contentElement.getAttributeValue("version"));
        String lastUpdateString = contentElement.getAttributeValue("lastUpdate");
        
        if (lastUpdateString != null) {
            Date lastUpdate = ISODateTimeFormat.dateTime().parseDateTime(lastUpdateString).toDate();
            descriptor.setLastUpdate(lastUpdate);
        }
        descriptor.setTitle(contentElement.getChildText("title", ns));
        descriptor.setInfo(contentElement.getChildText("info", ns));
        
        Element mediaElement = contentElement.getChild("media", ns);
        if (mediaElement != null) {
            String mimeType = mediaElement.getAttributeValue("mimeType", "unknown");
            descriptor.setMedia(mimeType, mediaElement.getText());
        }
        
        Element hintsElement = contentElement.getChild("hints", ns);
        if (hintsElement != null) {
            for (Element hintElement : hintsElement.getChildren("hint", ns)) {
                String text = hintElement.getText();
                descriptor.addHint(new Hint(text));
            }
        }
        
        Element warningsElement = contentElement.getChild("warnings", ns);
        if (warningsElement != null) {
            for (Element warningElement : warningsElement.getChildren("warning", ns)) {
                String text = warningElement.getText();
                String iconPath = warningElement.getAttributeValue("icon");
                descriptor.addWarning(new Warning(text, iconPath));
            }
        }
        
        String isRoutineString = contentElement.getChildText("isRoutine", ns);
        if (isRoutineString != null) {
            descriptor.setRoutineTask(Boolean.valueOf(isRoutineString));
        }
        
        return descriptor;
    }
    
    /**
     * Deserializes a JSON string as content descriptor. 
     * @param jsonString JSON string representing a content descriptor.
     * @return Content descriptor.
     * @throws IllegalArgumentException The given string is no valid representation of a content descriptor.
     */
    @SuppressWarnings("unchecked")
    public static ContentDescriptor readFromJSON(String jsonString) throws IllegalArgumentException {
        try {
            Map<String, Object> descriptorMap = JSON.std.mapFrom(jsonString);
            
            String id = (String) descriptorMap.get("id");
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing required [id].");
            }
            String languageId =(String) descriptorMap.get("lang");
            if (languageId == null || languageId.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing language identifier [lang].");
            }
            
            ContentDescriptor descriptor = new ContentDescriptor(id, languageId);
            descriptor.setVersion((String) descriptorMap.get("version"));
            String lastUpdateString = (String) descriptorMap.get("lastUpdate");
            if (lastUpdateString != null) {
                Date lastUpdate = ISODateTimeFormat.dateTime().parseDateTime(lastUpdateString).toDate();
                descriptor.setLastUpdate(lastUpdate);
            }
            descriptor.setTitle((String) descriptorMap.get("title"));
            descriptor.setInfo((String) descriptorMap.get("info"));
            
            String media = (String) descriptorMap.get("media");
            if (media != null) {
                String mimeType = (String) descriptorMap.get("mimeType");
                descriptor.setMedia(mimeType, media);
            }
            
            List<Map<String, Object>> hintsArr = (List<Map<String, Object>>) descriptorMap.get("hints");
            if (hintsArr != null) for (Map<String, Object> hintObj : hintsArr) {
                descriptor.addHint(new Hint((String) hintObj.get("text")));
            }
            
            List<Map<String, Object>> warningsArr = (List<Map<String, Object>>) descriptorMap.get("warnings");
            if (warningsArr != null) for (Map<String, Object> warningObj : warningsArr) {
                String icon = (String) warningObj.get("icon");
                String text = (String) warningObj.get("text");
                descriptor.addWarning(new Warning(text, icon));
            }
            Boolean isRoutine = (Boolean) descriptorMap.get("isRoutine");
            if (isRoutine) descriptor.setRoutineTask(isRoutine);
            return descriptor;
        } catch (JSONObjectException e) {
            throw new IllegalArgumentException("The given string is no valid JSON.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to access string.", e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Failed to parse field.", e);
        }
    }
    
    /**
     * Serializes a content descriptor as XML string.
     * @param descriptor Content descriptor to serialize.
     * @param compact If set to <code>true</code>, the XML will be encoded without unnecessary whitespaces.
     * @return XML string representing the content descriptor.
     */
    public static String writeAsXML(ContentDescriptor descriptor, boolean compact) {
        Namespace ns = Namespaces.CONTENTPACKAGE;
        Element contentElement = new Element("content", ns);
        contentElement.setAttribute("id", descriptor.getId());
        contentElement.setAttribute("lang", descriptor.getLanguageId());
        String version = descriptor.getVersion();
        if (version != null) contentElement.setAttribute("version", version);
        Date lastUpdate = descriptor.getLastUpdate();
        if (lastUpdate != null) {
            String lastUpdateString = ISODateTimeFormat.dateTime().print(new DateTime(lastUpdate));
            contentElement.setAttribute("lastUpdate", lastUpdateString);
        }
        String title = descriptor.getTitle();
        if (title != null) contentElement.addContent(new Element("title", ns).setText(title));
        String info = descriptor.getInfo();
        if (info != null) contentElement.addContent(new Element("info", ns).setText(info));
        String mediaPath = descriptor.getMediaPath();
        if (mediaPath != null) {
            String mimeType = descriptor.getMimeType();
            contentElement.addContent(new Element("media", ns).setAttribute("mimeType", mimeType).setText(mediaPath));
        }
        
        contentElement.addContent(new Element("isRoutine", ns).setText(descriptor.isRoutineTask() ? "true" : "false"));
        
        if (descriptor.getHints().size() > 0) {
            Element hintsElement = new Element("hints", ns);
            for (Hint hint : descriptor.getHints()) {
                hintsElement.addContent(new Element("hint", ns).setText(hint.getText()));
            }
            contentElement.addContent(hintsElement);
        }
        if (descriptor.getWarnings().size() > 0) {
            Element warningsElement = new Element("warnings", ns);
            for (Warning warning : descriptor.getWarnings()) {
                Element warningElement = new Element("warning", ns);
                String icon = warning.getIconPath();
                if (icon != null) {
                    warningElement.setAttribute("icon", icon);
                }
                warningElement.setText(warning.getText());
                warningsElement.addContent(warningElement);
            }
            contentElement.addContent(warningsElement);
        }
        
        return XMLUtils.exportAsString(contentElement, compact ? Format.getCompactFormat() : Format.getPrettyFormat());
    }
    
    /**
     * Serializes a content descriptor as JSON string.
     * @param descriptor Content descriptor.
     * @return JSON string representing the content descriptor. 
     */
    public static String writeAsJSON(ContentDescriptor descriptor) {
        try {
            JSONComposer<String> composer = new JSON().with(Feature.PRETTY_PRINT_OUTPUT).composeString();
            ObjectComposer<?> content = composer.startObject();
            content.put("id", descriptor.getId());
            content.put("lang", descriptor.getLanguageId());
            String version = descriptor.getVersion();
            if (version != null) content.put("version", version);
            Date lastUpdate = descriptor.getLastUpdate();
            if (lastUpdate != null) {
                String lastUpdateString = ISODateTimeFormat.dateTime().print(new DateTime(lastUpdate));
                content.put("lastUpdate", lastUpdateString);
            }
            String title = descriptor.getTitle();
            if (title != null) content.put("title", title);
            String info = descriptor.getInfo();
            if (info != null) content.put("info", info);
            String mediaPath = descriptor.getMediaPath();
            if (mediaPath != null) {
                String mimeType = descriptor.getMimeType();
                content.put("mimeType", mimeType);
                content.put("media", mediaPath);
            }
            content.put("isRoutine", descriptor.isRoutineTask());
            if (descriptor.getHints().size() > 0) {
                content.putObject("hints", descriptor.getHints());
            }
            if (descriptor.getWarnings().size() > 0) {
                ArrayComposer<?> warningsArr = content.startArrayField("warnings");
                for (Warning warning : descriptor.getWarnings()) {
                    ObjectComposer<?> warningObj = warningsArr.startObject();
                    String icon = warning.getIconPath();
                    if (icon != null) warningObj.put("icon", icon);
                    warningObj.put("text", warning.getText());
                    warningObj.end();
                }
                warningsArr.end();
            }
            content.end();
            return composer.finish();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
    }
}
