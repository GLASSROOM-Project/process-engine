package de.glassroom.gpe;

import java.util.Map;

public class Decision extends Tansition {
    
    private final Map<String, String> display;
    private final Condition condition;
    
    public Decision(Node<?> source, Node<?> target, Map<String, String> display, Condition condition) {
        super(source, target);
        this.display = display;
        this.condition = condition;
    }
    
    public Decision(String id, Node<?> source, Node<?> target, Map<String, String> display, Condition condition) {
        super(id, source, target);
        this.display = display;
        this.condition = condition;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public String getDisplay(String languageId) {
        return display.get(languageId);
    }
    
    public Map<String, String> getDisplays() {
        return display;
    }
}
