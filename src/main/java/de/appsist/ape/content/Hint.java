package de.glassroom.gpe.content;

/**
 * Model for a hint, i.e. a best practice to be made available for a step in a
 * guide.
 */
public class Hint {

    public String text;

    /**
     * Creates a new hint with the given (localized) text.
     *
     * @param text Text to be displayed as warning.
     */
    public Hint(String text) {
        this.text = text;
    }

    /**
     * Returns the text of the warning.
     *
     * @return Text to be displayed.
     */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
