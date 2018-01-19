package de.glassroom.gpe.content;

/**
 * Model for a warning.
 */
public class Warning {
    public String text;
    public final String iconPath;
    
    /**
     * Creates a new warning with the given (localized) text.
     * @param text Text to be displayed as warning.
     */
    public Warning(String text) {
        this(text, null);
    }
    
    /**
     * Creates a new warning with a text and an icon.
     * @param text Text to be displayed.
     * @param iconPath (Relative) path for the icon to be displayed.
     */
    public Warning(String text, String iconPath) {
        this.text = text;
        this.iconPath = iconPath;
    }
    
    /**
     * Returns the text of the warning.
     * @return Text to be displayed.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets the text of the warning.
     * @param text Text to be displayed.
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Returns the icon to be displayed with the warning.
     * @return Path of the icon or <code>null</code> if no icon is to be displayed.
     */
    public String getIconPath() {
        return iconPath;
    }
}
