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
package org.openhealthtools.mdht.mdmi.editor.map.tree;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.ModelChangeEvent;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.StatusPanel;
import org.openhealthtools.mdht.mdmi.editor.map.console.ReferenceLink;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AbstractComponentEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.AdvancedSelectionField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataEntryFieldInfo;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DefaultTextField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.GenericEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IEditorField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IntegerField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.SemanticElementField;
import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.editor.map.tools.NewNodeAndElementDialog;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ViewSyntaxNode;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.validate.ModelInfo;

public abstract class SyntaxNodeNode extends EditableObjectNode {

	/** Format string for "name : type" */
	public static final String s_nameAndTypeFormat = s_res.getString("EditableObjectNode.relationshipFormat");

	private String m_displayString = null;
	
	public SyntaxNodeNode(Node syntaxNode) {
		super(syntaxNode);
	}

	@Override
	public String getDisplayName(Object userObject) {
		String nodeName = ((Node)userObject).getName();
		if (nodeName == null || nodeName.length() == 0) {
			nodeName = ((Node)userObject).getFieldName();
		}
		return nodeName;
	}

	@Override
	public String getToolTipText() {
		return getSyntaxNode().getDescription();
	}

	/** Get the user object as a Node */
	public Node getSyntaxNode() {
		return (Node)getUserObject();
	}
	
	
	@Override
	public Object copyUserObject() throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Object userObjectCopy = super.copyUserObject();

		// don't copy SemanticElement/FieldName
		Node syntaxNode = (Node)userObjectCopy;
		syntaxNode.setSemanticElement(null);
		syntaxNode.setFieldName(null);
		
		return userObjectCopy;
	}
	

	@Override
	public boolean canDrag() {
		return true;
	}

	@Override
	public String toString() {
		Node node = getSyntaxNode();
		String display = getDisplayName();
		
		MdmiDatatype datatype = getDataType();
		//getSemanticElementType();
		if (datatype != null && datatype.getTypeName() != null) {

			// if node has a semantic element - show semantic element and datatype, otherwise
			//   if it has a fieldName, show the fieldName and datatype
			String otherElementName = null;
			if (node.getSemanticElement() != null) {
				otherElementName = node.getSemanticElement().getName();

			} else if (node.getFieldName() != null && node.getFieldName().length() > 0) {
				otherElementName = node.getFieldName();
			}

			if (otherElementName != null) {
				// field [datatype}
				String otherElement = MessageFormat.format(s_res.getString("SyntaxNodeNode.datatypeFormat"),
						otherElementName, datatype.getTypeName());
				// name : field [datatype]
				display = showNameAndType(display, otherElement);
			}
		}
		// since the display string can change when other nodes are edited (SemanticElement,
		// DataType or Field), we need to update the tree model if this happens 
		if (m_displayString != null && !display.equals(m_displayString)) {
			m_displayString = display;	// update before notification
			MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
			DefaultTreeModel model = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
			model.nodeChanged(this);
			entitySelector.repaint();
		} else {
			m_displayString = display;
		}
		return display;
	}
	
	/** Return a string showing a name and type, in the form "name : type" */
	public static String showNameAndType(String name, String type) {
		return MessageFormat.format(s_nameAndTypeFormat,
				name, type);
	}
	
	
	/** find the datatype of the semantic element or field name (they are mutually exclusive).
	 * 
	 * For example, assume there are the following data types
	 * <code>
	 *  ___________
	 * |    DT1    |      Data Types
	 * |-----------|
	 * |a1 : String|    ___________ 
	 * |a2 : DT2---|-->|    DT2    |
	 * |a3 : Binary|   |-----------|   
	 *  -----------    |b1 : Date  |
	 *                 |b2 : String|    ___________
	 *                 |b3 : DT3---|-->|    DT3    |
	 *                  -----------    |-----------|
	 *                                 |c1 : String|
	 *                                 |c2 : Date  |
	 *                                  -----------
	 * </code>
	 * Now assume there is a semantic element, SE1, that references Data Type DT1
	 * <code>
	 *     ____________
	 *    |    SE1     |
	 *    |------------|
	 *    |datatype DT1|
	 *     ------------
	 * </code>
	 * Finally there are syntax nodes, with the parent/child relationship as shown below.
	 * The display information is shown next to each node (node name, followed by the semantic
	 * element name or the field name (these are mutually exclusive), followed by the data type
	 * of the semantic element or field.
	 * <code>
	 *    ___________________
	 *   |  Bag bag1         |
	 *   |-------------------| bag1 : SE1 [DT1]
	 *   |semanticElement SE1|
	 *    -------------------
	 *       |     _____________
	 *       |----| Choice cho1 |
	 *            |-------------| cho1 : a2 [DT2]
	 *            |fieldName a2 |
	 *             -------------
	 *                |     _____________
	 *                |----| Bag bag2    |
	 *                     |-------------| bag2 : b3 [DT3]
	 *                     |fieldName b3 |
	 *                      -------------
	 *                         |     _____________
	 *                         |----| Leaf lf1    |
	 *                              |-------------| lf1 : c2 [Date]
	 *                              |fieldName c2 |
	 *                               -------------
	 * */
	
	public MdmiDatatype getDataType() {
		Node node = getSyntaxNode();
		// is there a semantic element
		if (node.getSemanticElement() != null) {
			return node.getSemanticElement().getDatatype();
		}
		
		// otherwise use the field name
		if (node.getFieldName() != null && node.getFieldName().length() > 0) {
			// get datatype from parent
			MdmiDatatype parentDatatype = getParentDatatype();
			if (parentDatatype instanceof DTComplex) {
				// get Field from datatype
				Field field = ((DTComplex)parentDatatype).getField(node.getFieldName());
				if (field != null) {
					return field.getDatatype();
				}
			}
		}
		
		return null;
	}
	
	/** get the datatype from the parent in the tree */
	private MdmiDatatype getParentDatatype() {
		if (getParent() instanceof  SyntaxNodeNode) {
			return ((SyntaxNodeNode)getParent()).getDataType();
		}
		return null;
	}
	
	/** get the semantic element from the parent node */
	private SemanticElement getEnclosingSemanticElement() {
		Node node = getSyntaxNode();
		if (node.getSemanticElement() != null) {
			return node.getSemanticElement();
		}
		if (getParent() instanceof SyntaxNodeNode) {
			return ((SyntaxNodeNode)getParent()).getEnclosingSemanticElement();
		}
		return null;
	}
	
	@Override
	public void setUserObject(Object userObject) {
		super.setUserObject(userObject);

		Node syntaxNode = (Node)userObject;
		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultTreeModel model = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();
		
		SemanticElement semanticElement = syntaxNode.getSemanticElement();
		if (semanticElement != null) {
			// make sure semantic element has this syntaxNode
			if (semanticElement.getSyntaxNode() != syntaxNode) {
				Node prevSyntaxNode = semanticElement.getSyntaxNode();
				// change semantic element to point here
				semanticElement.setSyntaxNode(syntaxNode);
				
				// update node display(s) to reflect semantic element
				DefaultMutableTreeNode syntaxNodeNode = entitySelector.findNode(semanticElement);
				if (syntaxNodeNode != null) {
					model.nodeChanged(syntaxNodeNode);
				}

				StatusPanel statusPanel = SelectionManager.getInstance().getStatusPanel();
				
				// This change has broken the link between one or more elements:
				String brokenLinkMsg = s_res.getString("EditableObjectNode.brokenLink");
				boolean messageShown = false;
				
				int count = 1;
				
				// produce warning for old syntax node
				if (prevSyntaxNode != null && prevSyntaxNode.getSemanticElement() == semanticElement) {
					EditableObjectNode prevNode = (EditableObjectNode)entitySelector.findNode(prevSyntaxNode);
					if (prevNode != null) {
						prevNode.setHasError(true);	// mark node with an error
					}
					// show error if previous element still references syntax node
					statusPanel.writeErrorText(brokenLinkMsg);
					messageShown = true;
					ReferenceLink link = new ReferenceLink(prevSyntaxNode, getDisplayName(prevSyntaxNode));
					link.addReferredToObject(semanticElement);

					// Broken Link: Syntax Node <LINK> references Semantic Element ''name'', 
					//  but Semantic Element ''name'' does not refer back to Syntax Node 'name'
					String preMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPreFormat"), 
							MdmiModelTree.formatItemNumber(count++), getDisplayType());
					String postMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPostFormat"),
							ClassUtil.beautifyName(semanticElement.getClass()), semanticElement.getName(),
							getDisplayType(), getDisplayName(prevSyntaxNode));
					
					statusPanel.writeErrorLink(preMsg, link, postMsg);
				}

				// warn if any Semantic Elements reference this Syntax Node
				List <EditableObjectNode> references = entitySelector.findReferences(this);
				for (EditableObjectNode node : references) {
					if (node instanceof SemanticElementNode && 
							((SemanticElement)node.getUserObject()).getSyntaxNode() == syntaxNode) {
						// show error
						if (!messageShown) {
							statusPanel.writeErrorText(brokenLinkMsg);
							messageShown = true;
						}
						if (node.getUserObject() == semanticElement) {
							// skip the one we just updated
							continue;
						}
						node.setHasError(true);	// mark node with an error
						ReferenceLink link = new ReferenceLink(node.getUserObject(), node.getDisplayName());
						link.addReferredToObject(syntaxNode);

						// Broken Link: Semantic Element <LINK> references Syntax Node 'name', 
						//  but Syntax Node 'name' does not refer back to Semantic Element 'name'
						String preMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPreFormat"), 
								MdmiModelTree.formatItemNumber(count++), node.getDisplayType());
						String postMsg = MessageFormat.format(s_res.getString("EditableObjectNode.brokenLinkPostFormat"),
								getDisplayType(), getDisplayName(syntaxNode),
								node.getDisplayType(), node.getDisplayName());

						statusPanel.writeErrorLink(preMsg, link, postMsg);

					}
				}
				
				// notify listeners to the semantic element
				SelectionManager.getInstance().notifyModelChangeListeners(semanticElement);
			}
		}
		
		// update all children since a change to the datatype will be reflected in the display
		for (Enumeration<?> en = preorderEnumeration(); en != null && en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)en.nextElement();
			if (child instanceof SyntaxNodeNode) {
				model.nodeChanged(child);
			}
		}
		
	}



	/** Create the appropriate SyntaxNodeNode for a syntax node  */
	public static SyntaxNodeNode createSyntaxNode(Node syntaxNode) {

		SyntaxNodeNode syntaxTreeNode = null;
		
		if (syntaxNode instanceof Bag) {
			Bag bag = (Bag)syntaxNode;
			syntaxTreeNode = new SyntaxBagNode(bag);
			
		} else if (syntaxNode instanceof Choice) {
			Choice choice = (Choice)syntaxNode;
			syntaxTreeNode = new SyntaxChoiceNode(choice);
			
		} else if (syntaxNode instanceof LeafSyntaxTranslator) {
			LeafSyntaxTranslator leaf = (LeafSyntaxTranslator)syntaxNode;
			syntaxTreeNode = new SyntaxLeafNode(leaf);
		}
		
		return syntaxTreeNode;
	}
	
	@Override
	public AbstractComponentEditor getEditorForNode() {
		return new CustomEditor(getMessageGroup(), getUserObject().getClass());
	}

	@Override
	public String getLocationExpressionLanguage() {
		String language = ((Node)userObject).getLocationExpressionLanguage();
		if (language == null || language.length() == 0) {
			language = getDefaultLocationExpressionLanguage();
		}
		return language;
	}

	public String getDefaultLocationExpressionLanguage() {
		String language = super.getLocationExpressionLanguage();
		return language;
	}


	@Override
	public List<JComponent> getAdditionalPopuMenus() {
		List<JComponent> menus = super.getAdditionalPopuMenus();
		if (menus == null) {
			menus = new ArrayList<JComponent>();
		}

		final Node node = getSyntaxNode();
		
		// Add New Node and Element
		if (node instanceof Bag || node instanceof Choice) {
			String title = s_res.getString("SyntaxNodeNode.newNodesAndSE");
			Icon icon =  AbstractComponentEditor.getIcon(this.getClass(),
					s_res.getString("SyntaxNodeNode.newNodesAndSEIcon"));
			menus.add(new JMenuItem(new AbstractAction(title, icon) {

				@Override
				public void actionPerformed(ActionEvent e) {
					Frame frame = SystemContext.getApplicationFrame();
					NewNodeAndElementDialog dlg = new NewNodeAndElementDialog(frame, node);
					dlg.centerInComponent(frame);
					dlg.setVisible(true);
				}

			}));
			menus.add(new JPopupMenu.Separator());
		}
		
		// View Syntax Node
		menus.add(new JMenuItem(new AbstractAction(s_res.getString("SyntaxNodeNode.viewHeirarchy")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				ViewSyntaxNode view = new ViewSyntaxNode(node);
				view.setVisible(true);
			}
			
		}));
		
		// Allow datatype to be opened
		MdmiDatatype datatype = getDataType();
		SemanticElement semanticElement = node.getSemanticElement();
		if (datatype != null) {
			menus.add(new JPopupMenu.Separator());
			if (semanticElement != null) {
				// Open Semantic Element
				menus.add( new JMenuItem(new OpenOtherAction(semanticElement)) );
			} else {
				SemanticElement enclosingSemanticElement = getEnclosingSemanticElement();
				if (enclosingSemanticElement != null) {
					// open semantic element
					menus.add( new JMenuItem(new OpenOtherAction(enclosingSemanticElement)) );
				}
				if (node.getFieldName() != null && node.getFieldName().length() > 0) {
					// Open Field
					String fieldName = node.getFieldName();
					MdmiDatatype parentDatatype = getParentDatatype();
					if (parentDatatype instanceof DTComplex) {
						Field field = ((DTComplex)parentDatatype).getField(fieldName);
						if (field != null) {
							menus.add( new JMenuItem(new OpenOtherAction(field)) );
						}
					}
				}
			}
			// Open Datatype
			menus.add( new JMenuItem(new OpenOtherAction(datatype)) );
		}
		return menus;
	}

	//////////////////////////////////////////////////////////////////
	//    Custom Classes
	//////////////////////////////////////////////////////////////
	public class CustomEditor extends AbstractModelChangeEditor {
		private DefaultTextField m_locationLanguageField;
		
		private static final String FIELD_NAME = "fieldName";

		public CustomEditor(MessageGroup group, Class<?> objectClass) {
			super(group, objectClass);
		}
		
		/** Determine if this field should be shown read-only */
		@Override
		public boolean isReadOnlyFields(String fieldName) {
			// ParentNode is read-only
			if ("ParentNode".equalsIgnoreCase(fieldName)) {
				return true;
			}
			if ( FIELD_NAME.equalsIgnoreCase(fieldName)) {
				// fieldName is read-only if there's a semantic element
				Node node = (Node)getUserObject();
				if (node.getSemanticElement() != null) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void highlightField(Object value, Color highlightColor) {
			// if value is a Field, we need to highlight the field name
			if (value instanceof Field) {
				IEditorField editorField = getEditorField(FIELD_NAME);
				try {
					editorField.highlightText(((Field)value).getName(), highlightColor);
				} catch (Exception e) {
					// ignore
				}
			} else {
				super.highlightField(value, highlightColor);
			}
		}

		@Override
		public void modelChanged(ModelChangeEvent e) {
			if (e.getSource() != getUserObject()) {
				// Could be a change to the language - update value
				String defaultLanguage = getDefaultLocationExpressionLanguage();
				m_locationLanguageField.setDefaultValue(defaultLanguage);
				
				return;
			}
			
			super.modelChanged(e);
		}

		/** Use a combo-box for fieldName values */
		@Override
		protected IEditorField createEditorField(DataEntryFieldInfo fieldInfo) {
			if (FIELD_NAME.equalsIgnoreCase(fieldInfo.getFieldName())) {
				// Use a combo-box
				return new FieldNameSelector(this);
				
			} else if ("LocationExpressionLanguage".equalsIgnoreCase(fieldInfo.getFieldName())) {
				// Show default
				m_locationLanguageField = new DefaultTextField(this, 
						getDefaultLocationExpressionLanguage());
				return m_locationLanguageField;
			
			} else if ("MaxOccurs".equalsIgnoreCase(fieldInfo.getFieldName())) {
				IEditorField field = super.createEditorField(fieldInfo);
				if (field instanceof IntegerField) {
					((IntegerField)field).addUnboundedBox();
				}
				return field;
				
			} else if ("SemanticElement".equalsIgnoreCase(fieldInfo.getFieldName())) {
				// This seField will allow us to create a new one
				SemanticElementField seField = new SemanticElementField(this);
				// set model context
				Node node = (Node)getUserObject();
				seField.setNode(node);
				return seField;
			}
			
			return super.createEditorField(fieldInfo);
		}

		@Override
		public List<ModelInfo> validateModel() {
			List<ModelInfo> errors = super.validateModel();
			
			// check min and max
			Node node = (Node)getEditObject();
			if (node.getMaxOccurs() < node.getMinOccurs()) {
				errors.add( new ModelInfo(node, "maxOccurs", 
						// maxOccurs must be >= minOccurs"
						MessageFormat.format(EditableObjectNode.s_res.getString("EditableObjectNode.minMaxError"),
								"maxOccurs", "minOccurs")
						) );
			}
			
			return errors;
		}
	}
	
	protected class FieldNameSelector extends AdvancedSelectionField {

		protected FieldNameSelector(GenericEditor parentEditor) {
			super(parentEditor);
		}

		@Override
		public Class<?> getDataClass() {
			return Field.class;
		}

		@Override
		protected Collection<? extends Object> getComboBoxData() {
			// Find all the allowed field names, based on the parent's type
			ArrayList<Field> elements = new ArrayList<Field>();

			MdmiDatatype datatype = getParentDatatype();
			if (datatype instanceof DTComplex) {
				for (Field field : ((DTComplex)datatype).getFields()) {
					// skip fields without valid names
					if (field.getName() != null && field.getName().length() > 0) {
						elements.add(field);
					}
				}
			}
			
			// Sort by name
			Collections.sort(elements, new Comparators.FieldComparator());
		
			List<Object> data = new ArrayList<Object>();
			data.addAll(elements);
			// make first item blank
			data.add(0, BLANK_ENTRY);
			return data;
		}

		@Override
		protected String toString(Object listObject) {
			if (listObject instanceof Field) {
				Field field = (Field)listObject;
				if (field.getDatatype() != null) {
					// name : Type
					return showNameAndType(field.getName(), field.getDatatype().getTypeName());	//field.getDatatype().getName());
				}
				return field.getName();
			}
			return listObject.toString();
		}
		
		/** Get a tooltip for an item in the list */
		@Override
		protected String getToolTipText(Object listObject) {
			if (listObject instanceof Field) {
				Field element = (Field)listObject;
				return element.getDescription();
			}
			return null;
		}

		@Override
		public Object getValue() {
			// return name only
			Object value = super.getValue();
			if (value instanceof Field) {
				value = ((Field)value).getName();
			}
			return value;
		}

		@Override
		public void setDisplayValue(Object value) {
			// value will be a string, but our list contains Fields
			if ("".equals(value)) {
				value = BLANK_ENTRY;
			} else if (value instanceof String) {
				// find Field with this name
				for (int i = 0; i < getComboBox().getItemCount(); i++) {
					Object item = getComboBox().getItemAt(i);
					if (item instanceof Field && value.equals(((Field)item).getName())) {
						value = item;
						break;
					}
				}
			}
			super.setDisplayValue(value);
		}
		
	}

	public static abstract class AbstractNewBag extends NewObjectInfo {
		/** return the type of child that will be created */
		@Override
		public Class<?> getChildClass() {
			return Bag.class;
		}
		
		/** Add this child object to its parent, and wrap it in a tree node */
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			Bag bag = (Bag)childObject;
			addNode(bag);
			return new SyntaxBagNode(bag);
		}

		/** Define how to add this Syntax node to its parent */
		public abstract void addNode(Bag node);

		@Override
		public String getChildName(Object childObject) {
			return ((Bag)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((Bag)childObject).setName(newName);
		}
	}
	
	public static abstract class AbstractNewChoice extends NewObjectInfo {
		
		/** return the type of child that will be created */
		@Override
		public Class<?> getChildClass() {
			return Choice.class;
		}
		
		/** Add this child object to its parent, and wrap it in a tree node */
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			Choice choice = (Choice)childObject;
			addNode(choice);
			return new SyntaxChoiceNode(choice);
		}
		
		/** Define how to add this Syntax node to its parent */
		public abstract void addNode(Choice node);

		@Override
		public String getChildName(Object childObject) {
			return ((Choice)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((Choice)childObject).setName(newName);
		}
	}
	
	public static abstract class AbstractNewLeaf extends NewObjectInfo {

		/** return the type of child that will be created */
		@Override
		public Class<?> getChildClass() {
			return LeafSyntaxTranslator.class;
		}
		
		/** Add this child object to its parent, and wrap it in a tree node */
		@Override
		public EditableObjectNode addNewChild(Object childObject) {
			LeafSyntaxTranslator leaf = (LeafSyntaxTranslator)childObject;
			addNode(leaf);
			return new SyntaxLeafNode(leaf);
		}
		
		/** Define how to add this Syntax node to its parent */
		public abstract void addNode(LeafSyntaxTranslator node);

		@Override
		public String getChildName(Object childObject) {
			return ((LeafSyntaxTranslator)childObject).getName();
		}

		@Override
		public void setChildName(Object childObject, String newName) {
			((LeafSyntaxTranslator)childObject).setName(newName);
		}
	}
	
	protected class OpenOtherAction extends AbstractAction {
		private Object m_otherObject;
		
		public OpenOtherAction(MdmiDatatype datatype) {
			putValue(AbstractAction.NAME, getText(datatype, datatype.getName()));
			m_otherObject = datatype;
		}
		
		public OpenOtherAction(SemanticElement se) {
			putValue(AbstractAction.NAME, getText(se, se.getName()));
			m_otherObject = se;
		}
		
		public OpenOtherAction(Field field) {
			putValue(AbstractAction.NAME, getText(field, field.getName()));
			m_otherObject = field;
		}
		
		private String getText(Object object, String name) {
			// Open Semantic Element se1
			return MessageFormat.format(s_res.getString("SyntaxNodeNode.openTypeFormat"), 
					ClassUtil.beautifyName(object.getClass()), name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// find and open datatype or field
			DefaultMutableTreeNode treeNode = SelectionManager.getInstance().getEntitySelector().findNode(m_otherObject);
			if (treeNode != null) {
				// select it (this will expand if necessary)
				SelectionManager.getInstance().getEntitySelector().selectNode(treeNode);
				SelectionManager.getInstance().editItem(treeNode);
			}

			
		}
		
	}
}
