package de.glassroom.gpe.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model for a content package.
 * @author simon.schwantzer(at)im-c.de
 */
public class ContentDescriptor {
        
    private final String id;
    private final String languageId;
    private final List<Warning> warnings;
    private final List<Hint> hints;
    
    private String version;
    private Date lastUpdate;
    private String title;
    private String info;
    private boolean isRoutineTask;
    private String mimeType;
    private String mediaPath;
    
    public ContentDescriptor(String id, String languageId) {
        this.id = id;
        this.languageId = languageId;
        this.hints = new ArrayList<Hint>();
        this.warnings = new ArrayList<Warning>();
    }

    public String getVersion() {
        return version;
    }

    public ContentDescriptor setVersion(String version) {
        this.version = version;
        return this;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public ContentDescriptor setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ContentDescriptor setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public ContentDescriptor setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getLanguageId() {
        return languageId;
    }
    
    public ContentDescriptor addHint(Hint hint) {
        hints.add(hint);
        return this;
    }
    
    public List<Hint> getHints() {
        return hints;
    }
    
    public ContentDescriptor addWarning(Warning warning) {
        warnings.add(warning);
        return this;
    }
    
    public List<Warning> getWarnings() {
        return warnings;
    }

    public boolean isRoutineTask() {
        return isRoutineTask;
    }

    public ContentDescriptor setRoutineTask(boolean isRoutineTask) {
        this.isRoutineTask = isRoutineTask;
        return this;
    }
    
    /**
     * Sets the media object.
     * @param mimeType Mime type of the media object.
     * @param mediaPath Path of the media object.
     * @return This for chaining.
     */
    public ContentDescriptor setMedia(String mimeType, String mediaPath) {
        this.mimeType = mimeType;
        this.mediaPath = mediaPath;
        return this;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getMediaPath() {
        return mediaPath;
    }
    
    
}
