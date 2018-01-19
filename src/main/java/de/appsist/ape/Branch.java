package de.glassroom.gpe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model for a gateway.
 * A gateway should be the only element with multiple outgoing flows, i.e., decisions.
 * @author simon.schwantzer(at)im-c.de
 */
public class Branch extends Node<Branch> {
    private List<Decision> decisions;
    
    /**
     * Creates a new gateway.
     */
    public Branch() {
        super("exclusiveGateway");
        decisions = new ArrayList<Decision>();
    }
    
    @Override protected Branch getThis() { return this; }

    /**
     * Creates a new gateway with a given identifier.
     * @param id Identifier for the process element.
     */
    public Branch(String id) {
        super("exclusiveGateway", id);
        decisions = new ArrayList<Decision>();
    }
    
    /**
     * Adds a new decision, i.e. a possible flow based on a condition.
     * @param target Node the decision should lead to.
     * @param display Map of display texts with ISO language code as key and text to display as value.
     * @param condition Condition for the flow.  
     */
    public Branch addDecision(Node<?> target, Map<String, String> display, Condition condition) {
        Decision decision = new Decision(this, target, display, condition);
        addOutgoing(decision);
        decisions.add(decision);
        return this;
    }
    
    @Override
    public Branch addOutgoing(Tansition flow) throws IllegalArgumentException {
        if (!(flow instanceof Decision)) {
            throw new IllegalArgumentException("Gateway only accepts decisions as outgoing flows.");
        }
        decisions.add((Decision) flow);
        return super.addOutgoing(flow);
    }

}
