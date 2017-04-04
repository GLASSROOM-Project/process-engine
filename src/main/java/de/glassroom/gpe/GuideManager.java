package de.glassroom.gpe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manager for guides.
 * @author simon.schwantzer(at)im-c.de
 */
public class GuideManager {
    private final Map<String, Guide> guides;

    /**
     * Creates a new guide manager.
     */
    public GuideManager() {
        this.guides = new LinkedHashMap<String, Guide>();
    }
    
    /**
     * Creates a new guide to be managed by this manager.
     * @param id Identifier for the guide.
     * @return Created guide.
     * @throws IllegalArgumentException A guide with the given identifier already exsists.
     */
    public Guide createGuide(String id) throws IllegalArgumentException {
        if (guides.containsKey(id)) {
            throw new IllegalArgumentException("A process with id \"" + id + "\" already exists.");
        }
        Guide process = new Guide(id);
        guides.put(id, process);
        return process;
    }
    
    /**
     * Returns the identifiers of all managed guides.
     * @return Set of guide identifiers.
     */
    public Set<String> getGuideIds() {
        return guides.keySet();
    }
    
    /**
     * Returns a managed guide.
     * @param id Identifier of the guide.
     * @return Guide or <code>null</code> if no guide with the given identifier is managed.
     */
    public Guide getGuide(String id) {
        return guides.get(id);
    }
    
    /**
     * Removes a single guide. 
     * @param id Identifier of the guide to remove.
     */
    public void deleteGuide(String id) {
        guides.remove(id);
    }
    
    /**
     * Returns a list of guides.
     * @param filter Filter to apply. If <code>null</code>, all available guides are returned.
     * @param comparator Comparator to sort guides. If <code>null</code> the guides are ordered by insertion. 
     * @return List of guides. May be empty.
     */
    public List<Guide> getGuides(Filter<Guide> filter, Comparator<Guide> comparator) {
        List<Guide> newList = new ArrayList<Guide>();
        if (filter != null) {
            for (Guide guide : guides.values()) {
                if (filter.accept(guide)) {
                    newList.add(guide);
                }
            }
        } else {
            newList.addAll(guides.values());
        }
        if (comparator != null) {
            Collections.sort(newList, comparator);
        }
        return newList;
    }
    
    /**
     * Adds a single guide to be managed.
     * @param guide Guide to be managed.
     */
    public void addGuide(Guide guide) {
        guides.put(guide.getId(), guide);
    }
    
    public List<Step> serializeGuide(String guideId) {
        Guide guide = guides.get(guideId);
        
        Set<Node<?>> nextNodes = guide.getStart().getNextNodes();
        Node<?> firstNode = nextNodes.iterator().next();
        
        return getStepsForNode(firstNode);
    }
    
    public List<Step> getStepsForNode(Node<?> node) {
        List<Step> list = new ArrayList<Step>();
        if (node instanceof Step) {
            list.add((Step) node);
            Set<Node<?>> nextNodes = node.getNextNodes();
            list.addAll(getStepsForNode(nextNodes.iterator().next()));
        } else if (node instanceof Chapter) {
            Chapter chapter = (Chapter) node;
            String guideId = chapter.getCalledProcessId();
            if (guides.containsKey(guideId)) {
                list.addAll(serializeGuide(guideId));
            }
            Set<Node<?>> nextNodes = node.getNextNodes();
            list.addAll(getStepsForNode(nextNodes.iterator().next()));
        } else {
            // we're done
        }
        return list;
    }
}
