package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.console.LinkedObject;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;

/** A helper for displaying Semantic Elements in a parent-child relationship */
public class HierarchicalSemanticElementHelper {

	// keep track of what we've examined
	Map<SemanticElement, SemanticElementNode> m_seMap = null;
	
	EditableObjectNode m_root;
	
	public HierarchicalSemanticElementHelper() {
		m_seMap = new HashMap<SemanticElement, SemanticElementNode>();
	}

	public void createTree(EditableObjectNode root, Collection<SemanticElement> elements, boolean showRules) {
		m_root = root;
		m_seMap.clear();
		
		// check 'em out
		for (SemanticElement se : elements) {
			processSE(se, showRules);
		}
		
		// Go though the map - any nodes that don't have a parent should be added to the root
		for (SemanticElementNode seNode : m_seMap.values()) {
			if (seNode.getParent() == null) {
				root.addSorted(seNode);
			}
		}
	}
	
	private void processSE(SemanticElement element, boolean showRules) {
		// if we've already processed, we can stop now
		if (m_seMap.containsKey(element)) {
			return;
		}
		SelectionManager selectionManager = SelectionManager.getInstance(); 
		
		// make a node
		SemanticElementNode seNode = new SemanticElementNode(element, true, showRules);
		m_seMap.put(element, seNode);
		
		// is the parent in the map?
		SemanticElement parent = element.getParent();
		if (parent != null) {
			
			// sanity check - make sure parent contains child
			if (!parent.getChildren().contains(element)) {
				LinkedObject link = new LinkedObject(element, element.getName());
				selectionManager.writeError("Semantic Element", link,
						"has " + parent.getName() + " as a parent, but "  + parent.getName() + " does not contain "
								+ element.getName() + ". The Semantic Element will be added to the parent.");
				
				parent.addChild(element);
			}
			
			// if we have a parent node, add this node to the parent node
			SemanticElementNode parentNode = m_seMap.get(parent);
			if (parentNode != null) {
				parentNode.addSorted(seNode);
			}
		}
		
		// add each child's node to this node
		Collection<SemanticElement> children = new ArrayList<SemanticElement>();
		// make a copy, so removing a child doesn't mess up the list
		children.addAll(element.getChildren());
		for (SemanticElement childSE : children) {
			
			// sanity check - make sure child has this parent
			if (childSE.getParent() != element) {
				if (childSE.getParent() == null) {
					childSE.setParent(element);
				} else {
					// trust the child's data - remove from parent
					LinkedObject link = new LinkedObject(element, element.getName());
					selectionManager.writeError("Semantic Element", link,
							"has " + childSE.getName() + " as a child, but "  + childSE.getName() + " has "
									+ childSE.getParent().getName() + " as its parent. The child, " + childSE.getName() +
									" will be removed from " + element.getName());
					element.getChildren().remove(childSE);
				}
			}
			
			// we've already processed the child - just give it a new parent node
			SemanticElementNode childNode = m_seMap.get(childSE);
			if (childNode != null) {
				seNode.addSorted(childNode);
			}
			
		}
	}
}
