/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeNodeIcon;
import org.openhealthtools.mdht.mdmi.editor.map.tree.TreeUtility;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.Node;

/** A Tree view of a datatype, and all objects that reference it */
public class DatatypeUsageTree extends ModelTree {
	private static boolean s_showDebug = false;
	
	/** Create the tree for this datatype */
	public DatatypeUsageTree(MdmiDatatype datatype) {
		super(datatype);
	}
	
	/** Create a tree showing all datatypes */
	public DatatypeUsageTree(Collection<MdmiDatatype> datatypes) {
		super(datatypes);
	}

	@Override
	public DefaultTreeCellRenderer createTreeCellRenderer() {
		return new UsageTreeRenderer();
	}

	@Override
	protected JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = super.createPopupMenu();

		DefaultMutableTreeNode selection = getSelection();

		// Open node
		if (selection instanceof UserTreeNode) {
			popupMenu.add(new OpenSelectionAction(selection));
		}

		return popupMenu;
	}

	/** return a list of Usage objects - one for each datatype, sorted by datatype name */
	public static Collection<Usage> getUsage(Collection<MdmiDatatype> datatypes) {
		Collection<Usage> users = new ArrayList<Usage>();
		
		HashMap<MdmiDatatype, Usage> usageMap = createUsageMap(datatypes);

		// need to sort by name
		ArrayList<MdmiDatatype> sortedKeys = DatatypeUsageTree.getSortedKeys(usageMap);
		for (MdmiDatatype key : sortedKeys) {
			Usage usage = usageMap.get(key);
			users.add(usage);
		}
		
		return users;
	}

	/** return a Usage object for this datatype */
	public static Usage getUsage(MdmiDatatype datatype) {
		Collection<MdmiDatatype> datatypes = new ArrayList<MdmiDatatype>();
		datatypes.add(datatype);
		
		HashMap<MdmiDatatype, Usage> usageMap = createUsageMap(datatypes);

		// get this type from the map
		Usage usage = usageMap.get(datatype);

		return usage;
	}

	/** Create a map showing usage for each datatype */
	private static HashMap<MdmiDatatype, Usage> createUsageMap(Collection<MdmiDatatype> datatypes) {
		HashMap<MdmiDatatype, Usage> usageMap = new HashMap<MdmiDatatype, Usage>();

		CursorManager cm = CursorManager.getInstance(SystemContext.getApplicationFrame());
		try {
			cm.setWaitCursor();
			TreeModel treeModel = SelectionManager.getInstance().getEntitySelector().getMessageElementsTree().getModel();
			TreeUtility treeUtility = new TreeUtility((DefaultMutableTreeNode) treeModel.getRoot());

			// check each datatype
			for (MdmiDatatype datatype : datatypes) {
				determineUsage(datatype, treeUtility, usageMap, 0);
			}
		} finally {
			cm.restoreCursor();
		}
		
		return usageMap;
	}


	/** Return the keys of a usageMap, sorted by the datatype name
	 * @param usageMap
	 * @return
	 */
	private static ArrayList<MdmiDatatype> getSortedKeys(HashMap<MdmiDatatype, Usage> usageMap) {
		ArrayList <MdmiDatatype> sortedKeys = new ArrayList<MdmiDatatype>();
		sortedKeys.addAll(usageMap.keySet());
		
		Collections.sort(sortedKeys, MdmiDatatypeField.getDatatypeComparator());
		return sortedKeys;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected DefaultMutableTreeNode createRootNode(Object model) {
				
		DefaultMutableTreeNode root = null;
		
		if (model instanceof MdmiDatatype) {
			// single entry - this will become the root
			Usage usage = getUsage((MdmiDatatype)model);
			root = new UserTreeNode(usage);

		} else if (model instanceof Collection) {
			// create the tree with a special root node
			root = new DefaultMutableTreeNode(s_res.getString("ViewDatatypeTree.datatypes"));
			Collection<MdmiDatatype> datatypes = (Collection<MdmiDatatype>) model;
			for (MdmiDatatype datatype : datatypes) {
				Usage usage = getUsage(datatype);
				DefaultMutableTreeNode node = new UserTreeNode(usage);
				MdmiModelTree.addSorted(root, node);
			}
		}

		return root;
	}
	
	// for debugging
	private static String indent(int spaces) {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<spaces; i++) {
			buf.append("    ");
		}
		return buf.toString();
	}
	
	/** Determine what other objects reference this datatype, either directly, or through another datatype that
	 * references this one */
	private static Usage determineUsage(MdmiDatatype datatype, TreeUtility treeUtility,
			HashMap<MdmiDatatype, Usage> usageMap, int depth) {
		if (s_showDebug) System.out.println(indent(depth) + "Checking " + datatype.getTypeName());
		// is it in the map already
		Usage user = usageMap.get(datatype);
		if (user != null) {
			if (s_showDebug) System.out.println(indent(depth) + " - already mapped");
			return user;
		}

		// save it first, so we don't check the same type multiple times
		user = new Usage(datatype);
		usageMap.put(datatype, user);

		List<EditableObjectNode> references = treeUtility.findReferences(datatype, datatype.getOwner());
		for (EditableObjectNode ref : references) {
			Object referencingObject = ref.getUserObject();

			if (referencingObject instanceof DTSDerived) {
				if (s_showDebug) System.out.println(indent(depth) + " - checking referencing derived type");
				// the datatype we're looking at is the base type of a derived type
				Usage derivedUsage = determineUsage((DTSDerived)referencingObject, treeUtility,
						usageMap, depth+1);
				user.addUser(derivedUsage);

			} else if (referencingObject instanceof Field) {
				MdmiDatatype ownerType = ((Field)referencingObject).getOwnerType();
				if ( ownerType == datatype) {
					// skip the fields of this datatype
//					continue;
					Usage fieldUsage = determineUsage((Field)referencingObject, treeUtility);
					// don't add if there are no users
					if (fieldUsage.getUsers().size() > 0) {
						user.addUser(fieldUsage);
					}
				} else {
					// need to check owner datatype
					if (s_showDebug) System.out.println(indent(depth) + " - checking owner type for field " + ((Field)referencingObject).getName());
					Usage ownerUsage = determineUsage(ownerType, treeUtility, usageMap, depth+1);
					// add owner
					user.addUser(ownerUsage);
				}

			} else {
				if (s_showDebug) System.out.println(indent(depth) + " - adding simple object - " + referencingObject.getClass().getSimpleName() +  " " + ClassUtil.getItemName(referencingObject));
				user.addUser(new Usage(referencingObject));
			}
		}

		return user;
	}


	/** Determine what other objects reference this field */
	private static Usage determineUsage(Field field, TreeUtility treeUtility) {
		Usage user = new Usage(field);

		List<EditableObjectNode> references = treeUtility.findReferences(field, field.getOwnerType().getOwner());
		for (EditableObjectNode ref : references) {
			Object referencingObject = ref.getUserObject();
			// skip datatypes to prevent infinite looping
			if (referencingObject instanceof MdmiDatatype) {
				continue;
			}
			user.addUser(new Usage(referencingObject));
		}

		return user;
	}

	///////////////////////////////////////////////////////////
	//  Tree Nodes
	///////////////////////////////////////////////////////////
	
//	public static interface IDatatypeField extends TreeNode {
//		public String getFieldName();
//		public MdmiDatatype getDatatype();
//	}
//
//	public static class DatatypeTreeNode extends DataTypeNode implements IDatatypeField {
//		private String m_fieldName;
//		
//		public DatatypeTreeNode(MdmiDatatype datatype, String fieldName) {
//			super(datatype);
//			m_fieldName = fieldName;
//		}
//		
//		@Override
//		public String toString() {
//			return DatatypeTree.formatFieldName(getDatatype(), m_fieldName);
//		}
//		
//		@Override
//		public MdmiDatatype getDatatype() {
//			return (MdmiDatatype)getUserObject();
//		}
//		
//		@Override
//		public String getFieldName() {
//			return m_fieldName;
//		}
//	}
	
	
	public static class UsageTreeRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);

			if (value instanceof UserTreeNode) {
				setIcon(((UserTreeNode)value).getNodeIcon());
				setToolTipText(((UserTreeNode)value).getToolTipText());
			} else {
				setIcon(null);
				setToolTipText(null);
			}
			return c;
		}
		
	}
	
	public static class UserTreeNode extends DefaultMutableTreeNode {
		private Icon m_icon;
		private String m_text;
		private String m_tooltip;
		
		/* -->  right arrow */
		public static final char RIGHT_ARROW  = '\u2192';
		
		public UserTreeNode(Usage user) {
			super(user.getModel());
			Object model = user.getModel();
			m_text = ClassUtil.getItemName(model);
			if (model instanceof Field) {
				m_text = DatatypeTree.formatFieldName(((Field)model).getDatatype(), m_text);
			} else if (model instanceof Node) {
				m_text = createSyntaxNodeText((Node)model);
			}
			
			m_icon = TreeNodeIcon.getTreeIcon(model.getClass());
			
			m_tooltip = ClassUtil.createToolTip(model);
			
			loadChildren(user);
		}
		
		/** Show full path on a syntax node: N1 -> N2 -> N3 (text will be wrapped in html tags)*/
		public static String createSyntaxNodeText(Node node) {
			StringBuilder buf = new StringBuilder();
			while(node != null) {
				buf.insert(0, node.getName());
				
				node = node.getParentNode();
				if (node != null) {
					buf.insert(0, RIGHT_ARROW);
				}
			}
			
			buf.insert(0, "<html>");
			buf.append("</html>");
			
			return buf.toString();
		}
		
		/** Add child nodes for each user */
		private void loadChildren(Usage user) {
			for (Usage subUser : user.getUsers()) {
				UserTreeNode childNode = new UserTreeNode(subUser);
				add(childNode);
			}
		}

		@Override
		public String toString() {
			return m_text;
		}
		
		public Icon getNodeIcon() {
			return m_icon;
		}
		
		public String getToolTipText() {
			return m_tooltip;
		}
	}
	
	/** Heirarchical usage. Object contains a list of other objects that use this one */
	public static class Usage {
		private Object m_model;
		private List<Usage> m_usedBy = new ArrayList<Usage>();
		
		public Usage(Object model) {
			m_model = model;
		}
		
		public Object getModel() {
			return m_model;
		}

		public void addUser(Usage user) {
			// check for duplicates
			for (Usage existing : m_usedBy) {
				if (existing.m_model == user.m_model) {
					return;
				}
			}
			m_usedBy.add(user);
		}
		
		
		/** Return all objects that reference this model */
		public Collection<Usage> getUsers() {
			return m_usedBy;
		}


		public boolean isInMessageModel() {
			if (!(m_model instanceof MdmiBusinessElementReference) &&
					!(m_model instanceof MdmiDatatype) &&
					!(m_model instanceof Field)) {
				return true;
			}
			// check references
			for (Usage user : getUsers()) {
				if (user.isInMessageModel()) {
					return true;
				}
			}
			return false;
		}

		public boolean isInBusinessReference() {
			if (m_model instanceof MdmiBusinessElementReference) {
				return true;
			}
			// check references
			for (Usage user : getUsers()) {
				if (user.isInBusinessReference()) {
					return true;
				}
			}
			return false;
		}

	}

}
