package de.glassroom.gpe;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.glassroom.gpe.annotations.ContentAnnotation;
import de.glassroom.gpe.annotations.MetadataAnnotation;
import de.glassroom.gpe.utils.IdGenerator;

/**
 * Abstract class for process nodes.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class Node<T extends Node<T>> {
    
    private final Map<Node<?>, Tansition> previousNodes;
    private final Map<Node<?>, Tansition> nextNodes;
    private MetadataAnnotation metadata;
    private ContentAnnotation content;
    private String name;
    
    private Guide parentGuide;
    private final String id;
    
    protected Node(String elementName) {
        this(elementName, IdGenerator.generateId(elementName + "-"));
    }
    
    protected Node(String elementName, String id) {
        this.id = id;
        
        this.previousNodes = new LinkedHashMap<>();
        this.nextNodes = new LinkedHashMap<>();
    }
    
    public String getId() {
        return id;
    }
    
    protected abstract T getThis();

    /**
     * Adds an successor for this node.
     * The flow between the nodes will be generated automatically.
     * @param target Node to add as successor.
     * @return This for chaining.
     */
    public T addNext(Node<? extends Node<?>> target) {
        if (!nextNodes.containsKey(target)) {
            Tansition outgoingFlow = new Tansition(this, target);
            nextNodes.put(target, outgoingFlow);

            target.setIncoming(this, outgoingFlow);
        }
        return getThis();
    }
    
    /**
     * Manually adds an outgoing flow.
     * @param flow Flow to add.
     * @return This for chaining.
     */
    public T addOutgoing(Tansition flow) {
        if (flow.getTarget() instanceof Node<?>) {
            nextNodes.put((Node<?>) flow.getTarget(), flow);
        }
        return getThis();
    }
    
    private T setOutgoing(Node<? extends Node<?>> target, Tansition flow) {
        if (flow != null) {
            nextNodes.put(target, flow);
        } else {
            nextNodes.remove(target);
        }
        return getThis();
    }
    
    /**
     * Returns all outgoing flows.
     * @return Collection of outgoing flows.
     */
    public Collection<Tansition> getOutgoing() {
        return nextNodes.values();
    }
    
    /**
     * Removes a successor of this node.
     * @param target Successor to remove.
     * @return This for chaining.
     */
    public T removeNext(Node<? extends Node<?>> target) {
        if (nextNodes.containsKey(target)) {
            target.setIncoming(this, null);
            nextNodes.remove(target);
        }
        return getThis();
    }
    
    /**
     * Adds a predecessor for this node.
     * The flow between the nodes is created automatically.
     * @param source Predecessor to add.
     * @return This for chaining.
     */
    public T addPrevious(Node<? extends Node<?>> source) {
        if (!previousNodes.containsKey(source)) {
            Tansition incomingFlow = new Tansition(source, this);
            previousNodes.put(source, incomingFlow);
            
            source.setOutgoing(this, incomingFlow);
        }
        return getThis();
    }
    
    /**
     * Manually adds an incoming flow.
     * @param flow Flow to add.
     * @return This for chaining.
     */
    public T addIncoming(Tansition flow) {
        if (flow.getTarget() instanceof Node<?>) {
            previousNodes.put((Node<?>) flow.getSource(), flow);
        }
        return getThis();
    }
    
    private T setIncoming(Node<? extends Node<?>> source, Tansition flow) {
        if (flow != null) {
            previousNodes.put(source, flow);
        } else {
            previousNodes.remove(source);
        }
        return getThis();
    }
    
    /**
     * Returns all incoming flows.
     * @return Collection of all incoming flows.
     */
    public Collection<Tansition> getIncoming() {
        return previousNodes.values();
    }
    
    /**
     * Removes a predecessor of this node.
     * @param source Predecessor to remove.
     * @return This for chaining.
     */
    public T removePrevious(Node<? extends Node<?>> source) {
        if (previousNodes.containsKey(source)) {
            source.setOutgoing(this, null);
            previousNodes.remove(source);
        }
        return getThis();
    }
    
    /**
     * Returns a set with all successors of this node.
     * @return Set of successors. May be empty.
     */
    public Set<Node<?>> getNextNodes() {
        return Collections.unmodifiableSet(nextNodes.keySet());
    }
    
    /**
     * Returns a set with all predecessors of this node.
     * @return Set of predecessors. May be empty.
     */
    public Set<Node<?>> getPreviousNodes() {
        return Collections.unmodifiableSet(previousNodes.keySet());
    }
    
    /**
     * Sets the meatadata for this node.
     * Existing metadata will be overwritten.
     * @param metadata Metadata annotation.
     * @return This for chaining.
     */
    public T setMetadata(MetadataAnnotation metadata) {
        this.metadata = metadata;
        return getThis();
    }
    
    /**
     * Returns the metadata of this node.
     * @return Metadata annotation or <code>null</code> if not set.
     */
    public MetadataAnnotation getMetadata() {
        return metadata;
    }
    
    /**
     * Sets the title for this node.
     * Automatically creates the related metadata constructs.
     * @param languageId ISO language code.
     * @param title Localized title to set.
     * @return This for chaining.
     */
    public T setTitle(String languageId, String title) {
        if (metadata == null) {
            metadata = new MetadataAnnotation();
        }
        metadata.setTitle(languageId, title);
        return getThis();
    }
    
    /**
     * Sets the descriptor for this node.
     * Automatically creates the related metadata constructs.
     * @param languageId ISO language code.
     * @param description Localized description to set.
     * @return This for chaining.
     */
    public T setDescription(String languageId, String description) {
        if (metadata == null) {
            metadata = new MetadataAnnotation();
        }
        metadata.setDescription(languageId, description);
        return getThis();
    }
    
    /**
     * Sets the content to be attached to this node.
     * Existing content will be overwritten.
     * @param content Content to set.
     * @return This for chaining.
     */
    public T setContent(ContentAnnotation content) {
        this.content = content;
        return getThis();
    }
    
    /**
     * Returns the content attached to this node.
     * @return Content annotation or <code>null</code> if not set.
     */
    public ContentAnnotation getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
            .append("Node(").append(getId());
        if (previousNodes.size() > 0) {
            builder.append("; incoming:");
            for (Node<?> prev : previousNodes.keySet()) {
                builder.append(" ").append(prev.getId());
            }
        }
        if (nextNodes.size() > 0) {
            builder.append("; outgoing:");
            for (Node<?> next : nextNodes.keySet()) {
                builder.append(" ").append(next.getId());
            }
        }
        builder.append(")");
        return builder.toString();
    }
    
    /**
     * Sets the name for the process element.
     * @param name Name to set.
     * @return This for chaining.
     */
    public T setName(String name) {
        this.name = name;
        return getThis();
    }
    
    /**
     * Returns the name for the process element.
     * @return Name to be displayed when editing the process.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the parent guide.
     * @param guide Guide to set as parent.
     */
    public void setParentGuide(Guide guide) {
        this.parentGuide = guide;
    }
    
    /**
     * Returns the parent guide.
     * @return Parent guide if set, otherwise <code>null</code>.
     */
    public Guide getParentGuide() {
        return parentGuide;
    }
    
    

}
