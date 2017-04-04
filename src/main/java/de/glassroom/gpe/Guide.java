package de.glassroom.gpe;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.glassroom.gpe.annotations.MetadataAnnotation;
import java.util.HashSet;

/**
 * Model for a assistance process.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class Guide {
    
    private final String id;
    private final Map<String, Node<?>> nodes;
    private Node<?> activeNode;
    private MetadataAnnotation metadata;
    
    /**
     * Creates a new assistance process.
     * @param id Identifier for the process.
     */
    public Guide(String id) {
        this.id = id;
        
        nodes = new LinkedHashMap<>();
        
        GuideStart startEvent = new GuideStart();
        insertNode(startEvent);
        
        GuideEnd defaultEndEvent = new GuideEnd();
        insertNode(defaultEndEvent);
        
        activeNode = startEvent;
        metadata = new MetadataAnnotation();
        metadata.setLastUpdate(new Date());
        
        startEvent.addNext(defaultEndEvent);
    }
    
    /**
     * Creates a new assistance process based on a given graph of nodes. 
     * @param id Identifier for the process.
     * @param nodes Map of nodes with id as key and node object as value.
     * @throws IllegalArgumentException The given set of nodes does not contain an process end.
     */
    public Guide(String id, Map<String, Node<?>> nodes) throws IllegalArgumentException {
        this.id = id;
        this.nodes = nodes;
        for (Node<?> node : nodes.values()) {
            node.setParentGuide(this);
            if (node instanceof GuideEnd) {
                Iterator<Node<?>> secondLastNodes = node.getPreviousNodes().iterator();
                if (secondLastNodes.hasNext()) {
                    activeNode = secondLastNodes.next();
                    // break;
                }
            }
        }
        if (activeNode == null) {
            throw new IllegalArgumentException("The given set of nodes does not contain an (reachable) process end."); 
        }
    }
    
    private void insertNode(Node<?> newNode) {
        nodes.put(newNode.getId(), newNode);
        newNode.setParentGuide(this);
        if (activeNode != null) {
            Set<Node<?>> nextNodes = activeNode.getNextNodes();
            for (Node<?> node : nextNodes) {
                activeNode.removeNext(node);
                newNode.addNext(node);
            }
            activeNode.addNext(newNode);
        }
        activeNode = newNode;
        update();
    }
    
    public void update() {
        if (metadata == null) {
            metadata = new MetadataAnnotation();
        }
        metadata.setLastUpdate(new Date());
    }
    
    /**
     * Adds a node to the process.
     * @param node Node to add.
     * @return This for chaining.
     */
    public Guide addNode(Node<?> node) {
        insertNode(node);
        return this;
    }
    
    /**
     * Adds a node after the a given predecessor.
     * The node will take over all successors from the predecessor will therefore be the only successor of the predecessor.
     * @param node Node to add.
     * @param predecessor Node to add the node after.
     * @return This for chaining.
     */
    public Guide addNode(Node<?> node, Node<?> predecessor) {
        activeNode = predecessor;
        insertNode(node);
        return this;
    }
    
    /**
     * Removes a node from the 
     * @param node Node to remove.
     * @return This for chaining.
     * @throws IllegalArgumentException Node is not part of the guide or cannot be removed based on its type.
     * @throws IllegalStateException A removal of the node will corrupt the guide structure and is therefore not possible.
     */
    public Guide removeNode(Node<?> node) throws IllegalArgumentException, IllegalStateException {
        if (!nodes.containsValue(node)) {
            throw new IllegalArgumentException("The given node is not part of guide: " + id);
        }
        
        if (node.getNextNodes().size() > 1) {
            throw new IllegalStateException("Cannot delete nodes with multiple successors.");
        }
        
        if (node instanceof GuideStart) {
            throw new IllegalArgumentException("Cannot remove start node.");
        }
        
        if (node instanceof GuideEnd && getEndNodes().size() < 2) {
            throw new IllegalStateException("Cannot remove single end node.");
        }
        
        for (Node<?> predecessor : node.getPreviousNodes()) {
            if (node == activeNode) {
                activeNode = predecessor;
            }
            predecessor.removeNext(node);
            for (Node<?> successor : node.getNextNodes()) {
                predecessor.addNext(successor);
                successor.addPrevious(predecessor);
            }
        }
        
        for (Node<?> successor : node.getNextNodes()) {
            successor.removePrevious(node);
        }
        node.setParentGuide(null);
        nodes.remove(node.getId());
        update();
        return this;
    }
    
    /**
     * Moves  a node as sucessor of another node.
     * @param nodeToMove Node to move.
     * @param newPredecessor New predecessor of the node.
     * @return This for chaining.
     * @throws IllegalArgumentException The node cannot be moved.
     * @throws IllegalStateException A movement of the node would corrupt the guide structure.
     */
    public Guide moveNode(Node<?> nodeToMove, Node<?> newPredecessor) throws IllegalArgumentException, IllegalStateException {
        if (!nodes.containsValue(nodeToMove)) {
            throw new IllegalArgumentException("The given node is not part of guide: " + id);
        }        
        if (newPredecessor instanceof GuideEnd) {
            throw new IllegalArgumentException("Cannot define a successor of an end node.");
        }
        removeNode(nodeToMove);
        addNode(nodeToMove, newPredecessor);
        return this;
    }
    
    /**
     * Returns the note marked as "active". If not told otherwise, new nodes will be added
     * between the active node and its successors.
     * 
     * The active node is always the node last added to the process.
     * 
     * @return Node marked as active.
     */
    public Node<?> getActiveNode() {
        return activeNode;
    }
    
    /**
     * Returns the ID of the process.
     * @return
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the metadata information for the process.
     * Existing metadata will be overwritten.
     * @param metadata Metadata for the process.
     * @return This for chaining.
     */
    public Guide setMetadata(MetadataAnnotation metadata) {
        this.metadata = metadata;
        update();
        return this;
    }
    
    public Guide setMetadataWithoutUpdate(MetadataAnnotation metadata) {
        this.metadata = metadata;
        return this;
    }
    
    /**
     * Returns the metadata of the process.
     * @return Metadata or <code>null</code> if not set.
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
    public Guide setTitle(String languageId, String title) {
        if (metadata == null) {
            metadata = new MetadataAnnotation();
        }
        metadata.setTitle(languageId, title);
        update();
        return this;
    }
    
    /**
     * Sets the descriptor for this node.
     * Automatically creates the related metadata constructs.
     * @param languageId ISO language code.
     * @param description Localized description to set.
     * @return This for chaining.
     */
    public Guide setDescription(String languageId, String description) {
        if (metadata == null) {
            metadata = new MetadataAnnotation();
        }
        metadata.setDescription(languageId, description);
        update();
        return this;
    }
    
    /**
     * Returns a node of the process.
     * @param id Node identifier.
     * @return Node or <code>null</code> if no node with the given identifier exists.
     */
    public Node<?> getNode(String id) {
        return nodes.get(id);
    }
    
    /**
     * Returns all nodes of the process.
     * @return List of nodes, may be empty.
     */
    public List<Node<?>> getNodes() {
        return new ArrayList<>(nodes.values());
    }
    
    public Set<GuideEnd> getEndNodes() {
        Set<GuideEnd> endNodes = new LinkedHashSet<>();
        for (Node<?> node : nodes.values()) {
            if (node instanceof GuideEnd) {
                endNodes.add((GuideEnd) node);
            }
        }
        return endNodes;
    }
    
    /**
     * Returns the last update. Updates do not include modifications of steps and chapters.
     * @return Date time of the last operation. May be <code>null</code>. 
     */
    public Date getLastUpdate() {
        return metadata != null ? metadata.getLastUpdate() : null;
    }
    
    /**
     * Returns the start event of the guide.
     * @return Start event of the guide.
     */
    public GuideStart getStart() {
        for (Node<?> node : nodes.values()) {
            if (node instanceof GuideStart) return (GuideStart) node;
        }
        return null;
    }
    
    /**
     * Returns all available paths through the guide.
     * Works only for tree-structured guides.
     * @return A list containing all pathes available through the guide.
     * @throws IllegalStateException A cycle has been detected.
     */
    public List<List<Node>> getAllPaths() throws IllegalStateException {
        return serializeGuide(getStart(), new HashSet<Node>());
    }
    
    private List<List<Node>> serializeGuide(Node node, Set<Node> predecessors) throws IllegalStateException {
        // TODO Resolve cyclic references with own data structure.
        HashSet<Node> visitedNodes = new HashSet<>();
        visitedNodes.addAll(predecessors);
        visitedNodes.add(node);
        
        List<List<Node>> paths = new ArrayList<>();
        if (node instanceof GuideEnd) {
            List<Node> endPath = new ArrayList<>();
            endPath.add(node);
            paths.add(endPath);
        } else {
            Set<Node<?>> nextNodes = node.getNextNodes();
            for (Node<?> nextNode : nextNodes) {
                if (visitedNodes.contains(nextNode)) {
                    throw new IllegalStateException("Guide cannot be serialized as it contains cycles. Detected cycle: " + node.getId() + " -> " + nextNode.getId());
                }
                List<List<Node>> nextPaths = serializeGuide(nextNode, visitedNodes);
                for (List<Node> nextPath : nextPaths) {
                    List<Node> pathWithCurrent = new ArrayList<>();
                    pathWithCurrent.add(node);
                    pathWithCurrent.addAll(nextPath);
                    paths.add(pathWithCurrent);
                }
            }
        }
        return paths;
    }
    
    /**
     * Combines one or more steps to a new guide and embed it as chapter.
     * @param newGuideId Identifier for the guide to be created.
     * @param nodesToCombine List of nodes to combine. The list may not be empty and may not have external references.
     * @return New guide contining the given nodes and referenced as chapter.
     * @throws IllegalArgumentException The list of nodes is empty or contains depencencies to external nodes.
     */
    public Guide combineStepsToChapter(String newGuideId, List<Node> nodesToCombine) throws IllegalArgumentException {
        if (nodesToCombine.isEmpty()) throw new IllegalArgumentException("The list of nodes to combine may not be empty.");
        if (!isClosure(nodesToCombine)) throw new IllegalArgumentException("Dependencies to external nodes are not resolved.");
        
        Node firstNode = nodesToCombine.get(0);
        Set<Node> predecessors = firstNode.getPreviousNodes();
        Node lastNode = nodesToCombine.get(nodesToCombine.size() - 1);
        Set<Node> successors = lastNode.getNextNodes();
        Chapter newChapter = new Chapter(newGuideId);
        nodes.put(newChapter.getId(), newChapter);
        
        Node startEvent = new GuideStart();
        Node endEvent = new GuideEnd();
        
        for (Node predecessor : predecessors) {
            firstNode.removePrevious(predecessor);
            newChapter.addPrevious(predecessor);
        }
        firstNode.addPrevious(startEvent);
        
        for (Node successor : successors) {
            lastNode.removeNext(successor);
            newChapter.addNext(successor);
        }
        lastNode.addNext(endEvent);
        
        Map<String, Node<?>> newGuideNodes = new LinkedHashMap<>();
        newGuideNodes.put(startEvent.getId(), startEvent);
        for (Node node : nodesToCombine) {
            nodes.remove(node.getId());
            newGuideNodes.put(node.getId(), node);
        }
        newGuideNodes.put(endEvent.getId(), endEvent);
        
        Guide newGuide = new Guide(newGuideId, newGuideNodes);
        return newGuide;
    }
    
    private boolean isClosure(List<Node> nodeList) {
        if (nodeList.size() >= 2) {
            for (int i = 0; i < nodeList.size() - 1; i++) {
                Set<Node> successors = nodeList.get(i).getNextNodes();
                for (Node<?> successor : successors) {
                    if (!nodeList.contains(successor)) return false;
                }
            }
            for (int i = 1; i < nodeList.size(); i++) {
                Set<Node> predecessors = nodeList.get(i).getPreviousNodes();
                for (Node<?> prodecessor : predecessors) {
                    if (!nodeList.contains(prodecessor)) return false;
                }
            }
        }
        return true;
    }
}
