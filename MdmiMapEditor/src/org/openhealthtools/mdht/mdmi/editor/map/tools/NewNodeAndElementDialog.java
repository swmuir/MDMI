package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CheckBoxListPanel;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataFormatException;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IntegerField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementSetNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SyntaxNodeNode;
import org.openhealthtools.mdht.mdmi.model.Bag;
import org.openhealthtools.mdht.mdmi.model.Choice;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.LeafSyntaxTranslator;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.SemanticElement;
import org.openhealthtools.mdht.mdmi.model.SemanticElementSet;
import org.openhealthtools.mdht.mdmi.model.enums.SemanticElementType;

/** wizard that creates a syntax node and semantic element in one step */
public class NewNodeAndElementDialog extends BaseDialog implements DocumentListener, ActionListener {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	private Node m_parentNode = null;

	// Syntax Node fields
	private JTextField  m_nodeName  = new JTextField();
	private JComboBox<ChildNodeWrapper> m_nodeTypes = new JComboBox<ChildNodeWrapper>();
	private JTextField  m_formatExpressionLanguage  = new JTextField();
	private JTextField  m_location  = new JTextField();
	private IntegerField  m_maxOccurs = new IntegerField(8);

	// Semantic Element fields
	private JComboBox<MessageModelWrapper> m_models = new JComboBox<MessageModelWrapper>();
	private JTextField  m_seName  = new JTextField();
	private JComboBox<SemanticElementType> m_seTypes = new JComboBox<SemanticElementType>();
	private JComboBox<Object> m_datatypes = new JComboBox<Object>();
	private CheckBoxListPanel m_fieldSelectionPanel = new CheckBoxListPanel();
	
	// [] Append '@' to Attribute
	private JCheckBox m_attrBox = new JCheckBox(s_res.getString("NewNodeAndElementDialog.attrButton"));
	
	public NewNodeAndElementDialog(Frame owner, Node parentNode) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		m_parentNode = parentNode;

		buildUI();
		setTitle(s_res.getString("NewNodeAndElementDialog.title"));
		pack(new Dimension(500,300));
	}

	
	private void buildUI() {
		//  - Syntax Node -----------------------
		// | Node Name:  [________]              |
		// | Type:       [__________|v]          |
		// | Location:   [________]              |
		// | Language:   [________]              |
		// | Max Occurs: [________] [x]Unbounded |
		//  -------------------------------------
		//  - Semantic Element ------------------
		// | Name:       [____________________]  |
		// | SE Type:    [__________________|v]  |
		// | Datatype:   [__________________|v]  |
		//  -------------------------------------
		//  [x] Prepend '@' to Attribute

		// Create Widgets
		JPanel syntaxNodePanel = createSyntaxNodePanel();
		JPanel semanticElementPanel = createSemanticElementPanel();
		
		////////////////////////////////////////////////////////////
		// put Node and SE Panels in one
		////////////////////////////////////////////////////////////
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		syntaxNodePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("NewNodeAndElementDialog.syntaxNode")));
		mainPanel.add(syntaxNodePanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		semanticElementPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("NewNodeAndElementDialog.semanticElement")));
		mainPanel.add(semanticElementPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		mainPanel.add(m_attrBox, gbc);

		////////////////////////////////////////////////////////////
		// add listeners
		////////////////////////////////////////////////////////////
		m_nodeName.getDocument().addDocumentListener(this);
		m_datatypes.addActionListener(this);
		
		setDirty(true);	// allow OK button
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}


	//  - Syntax Node -----------------------
	// | Node Name:  [________]              |
	// | Type:       [__________|v]          |
	// | Location:   [________]              |
	// | Language:   [________]              |
	// | Max Occurs: [________] [x]Unbounded |
	//  -------------------------------------
	private JPanel createSyntaxNodePanel() {
		JPanel syntaxNodePanel = new JPanel(new GridBagLayout());
		// add types to node types
		m_nodeTypes.addItem(new ChildNodeWrapper(Bag.class));
		m_nodeTypes.addItem(new ChildNodeWrapper(Choice.class));
		m_nodeTypes.addItem(new ChildNodeWrapper(LeafSyntaxTranslator.class));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 1;

		// Node Name: 
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.nodeName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		syntaxNodePanel.add(m_nodeName, gbc);

		// Type: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.nodeType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		syntaxNodePanel.add(m_nodeTypes, gbc);

		// Location: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.location")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		syntaxNodePanel.add(m_location, gbc);
		
		// Format Expression Language: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.formatExpressionLanguage")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		syntaxNodePanel.add(m_formatExpressionLanguage, gbc);
		
		// Max Occurs:
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.right = 0;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.maxOccurs")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		m_maxOccurs.addUnboundedBox();
		try {
			m_maxOccurs.setDisplayValue(Integer.MAX_VALUE);
		} catch (DataFormatException e) {
		}
		syntaxNodePanel.add(m_maxOccurs, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		return syntaxNodePanel;
	}


	//  - Semantic Element ------------------
	// | Name:       [____________________]  |
	// | SE Type:    [__________________|v]  |
	// | Datatype:   [__________________|v]  |
	// |  - Fields------------------------   |
	// | |  [x] Field 1                   |  |
	// | |  [x] Field 2                   |  |
	// | |  [x] Field 3                   |  |
	// |  --------------------------------   |
	//  -------------------------------------
	private JPanel createSemanticElementPanel() {
		JPanel semanticElementPanel = new JPanel(new GridBagLayout());
		
		// add se types
		for (SemanticElementType type : SemanticElementType.values()) {
			m_seTypes.addItem(type);
		}
		m_seTypes.setSelectedItem(SemanticElementType.NORMAL);
		
		// add datatypes
		m_datatypes.addItem(MdmiDatatypeField.BLANK_ENTRY);   // make first item blank
		MessageGroup group = m_parentNode.getSyntaxModel().getModel().getGroup();
		List<MdmiDatatype> dataTypes = MdmiDatatypeField.getAllDatatypes(group, MdmiDatatype.class);
		for (MdmiDatatype dataType : dataTypes) {
			m_datatypes.addItem(new MdmiDatatypeWrapper(dataType));
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 0;

		// Message Model: 
		if (group.getModels().size() > 1) {
			// Message Models
			for (MessageModel msgModel : group.getModels()) {
				m_models.addItem(new MessageModelWrapper(msgModel));
			}
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			semanticElementPanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.messageModel")), gbc);
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			semanticElementPanel.add(m_models, gbc);

			gbc.gridx = 0;
			gbc.gridy++;
		}

		// SE Name: 
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		semanticElementPanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.seName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		semanticElementPanel.add(m_seName, gbc);

		// SE Type: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		semanticElementPanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.seType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		semanticElementPanel.add(m_seTypes, gbc);

		// Data Type: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		semanticElementPanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.dataType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		semanticElementPanel.add(m_datatypes, gbc);
		
		// Fields
		JScrollPane scroller = new JScrollPane(m_fieldSelectionPanel);
		scroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				s_res.getString("NewNodeAndElementDialog.fields")));
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.weighty = 1;	// gets all the weight
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		semanticElementPanel.add(scroller, gbc);
		gbc.gridwidth = 1;
		
		return semanticElementPanel;
	}


	// fill in the Field Selection
	private void fillFieldSelectionList() {
		DefaultListModel<?> listModel = m_fieldSelectionPanel.getModel();
		listModel.removeAllElements();
		
		MdmiDatatype datatype = getDatatype();
		if (datatype instanceof DTComplex) {
			for (Field field : ((DTComplex)datatype).getFields()) {
				m_fieldSelectionPanel.addCheckBox(new FieldCheckBox(field));
			}
		}
	}

	@Override
	public void dispose() {
		m_nodeName.getDocument().removeDocumentListener(this);
		m_datatypes.removeActionListener(this);
		super.dispose();
	}
	
	@Override
	public boolean isDataValid() {
		// must have a Node name
		if (getNodeName().isEmpty()) {
			return false;
		}
		// must have an SE name
		if (getSemanticElementName().isEmpty()) {
			return false;
		}
		// must have a datatype
		if (getDatatype() == null) {
			return false;
		}
		// must have a message model
		if (getMessageModel() == null) {
			return false;
		}
		return true;
	}
	
	public String getNodeName() {
		return m_nodeName.getText().trim(); 
	}
	
	public String getSemanticElementName() {
		return m_seName.getText().trim();
	}
	
	public MdmiDatatype getDatatype() {
		Object selectedItem = m_datatypes.getSelectedItem();
		if (selectedItem instanceof MdmiDatatypeWrapper) {
			return ((MdmiDatatypeWrapper)selectedItem).dataType;
		}
		return null;
	}
	
	public int getMaxOccurs() {
		int intValue = 0;
		try {
			Object value = m_maxOccurs.getValue();
			if (value instanceof Integer) {
				intValue = ((Integer)value).intValue();
			}
		} catch (DataFormatException e) {
		}
		return intValue;
	}
	
	public MessageModel getMessageModel() {
		MessageGroup group = m_parentNode.getSyntaxModel().getModel().getGroup();
		MessageModel msgModel = null;
		Collection<MessageModel> messageModels = group.getModels();
		if (messageModels.size() > 1) {
			MessageModelWrapper sel = (MessageModelWrapper)m_models.getSelectedItem();
			msgModel = sel.messageModel;
		} else {
			// get first one
			for (MessageModel aModel : messageModels) {
				msgModel = aModel;
				break;
			}
		}
		return msgModel;
	}

	@Override
	protected void okButtonAction() {
		String nodeName = getNodeName();
		String semanticElementName = getSemanticElementName();
		int maxOccurs = getMaxOccurs();
		MdmiDatatype datatype = getDatatype();
		
		// Check uniqueness
		MessageModel msgModel = getMessageModel();
		SemanticElementSet semanticElementSet = msgModel.getElementSet();
		SemanticElement matchingSE = findSemanticElementByName(semanticElementSet, semanticElementName);
		Node matchingNode = findNodeByName(m_parentNode, nodeName);
		if (matchingNode != null) {
			// warn and quit
			String message = m_parentNode.getName() + " already contains a Syntax Node named '" + matchingNode.getName();
			message += "'\nPlease select a different name.";
			JOptionPane.showMessageDialog(SelectionManager.getInstance().getEntityEditor(), message,
					"Duplicate Name", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (matchingSE != null) {
			// warn and quit
			String message = "There is already a Semantic Element named '" + matchingSE.getName();
			message += "'\nPlease select a different name.";
			JOptionPane.showMessageDialog(SelectionManager.getInstance().getEntityEditor(), message,
					"Duplicate Name", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// 1. Create Node
		Node node = null;
		ChildNodeWrapper nodeType = (ChildNodeWrapper)m_nodeTypes.getSelectedItem();
		if (nodeType.m_theClass.equals(Bag.class)) {
			node = new Bag();
		} else if (nodeType.m_theClass.equals(Choice.class)) {
			node = new Choice();
		} else {
			node = new LeafSyntaxTranslator();
			((LeafSyntaxTranslator)node).setFormatExpressionLanguage(m_formatExpressionLanguage.getText().trim());
		}
		node.setName(nodeName);
		node.setLocation(m_location.getText().trim());
		node.setMinOccurs(0);
		node.setMaxOccurs(maxOccurs);
		
		addNodeToParent(m_parentNode, node);
		
		////////////////////////////////////////////////
		// 2. Create Semantic Element
		SemanticElement semanticElement = new SemanticElement();
		
		semanticElement.setName(semanticElementName);
		semanticElement.setDatatype(datatype);
		semanticElement.setMultipleInstances(maxOccurs > 1);
		semanticElement.setSemanticElementType((SemanticElementType)m_seTypes.getSelectedItem());
		
		semanticElementSet.addSemanticElement(semanticElement);
		semanticElement.setElementSet(semanticElementSet);

		////////////////////////////////////////////////
		// 3. Connect SE and Node
		semanticElement.setSyntaxNode(node);
		node.setSemanticElement(semanticElement);
		
		////////////////////////////////////////////////////
		// 4. Generate children
		if (datatype instanceof DTComplex && (node instanceof Bag || node instanceof Choice)) {
			for (JCheckBox checkBox : m_fieldSelectionPanel.getCheckBoxes()) {

		      	if (checkBox.isSelected()) {
		      		Field field = ((FieldCheckBox)checkBox).field;
		      		generateChildren(node, field);
		      	}
			}
		}
		
		///////////////////////////////////////////////////////
		// 5. Update Tree
		// notify
		SelectionManager.getInstance().setUpdatesPending();
		SelectionManager.getInstance().notifyCollectionChangeListeners(node.getClass());
		SelectionManager.getInstance().notifyCollectionChangeListeners(semanticElement.getClass());
	   
		updateMdmiTree(node, semanticElement);

		
		super.okButtonAction();
	}

	private Node findNodeByName(Node parent, String nodeName) {
		Node matching = null;
		
		ArrayList<Node> nodeList = null;
		if (m_parentNode instanceof Choice) {
			nodeList = ((Choice)m_parentNode).getNodes();
		} else if (m_parentNode instanceof Bag){
			nodeList = ((Bag)m_parentNode).getNodes();
		}
		if (nodeList != null) {
			for (Node other : nodeList) {
				if (nodeName.equalsIgnoreCase(other.getName())) {
					matching = other;
					return matching;
				}
			}
		}
		
		return null;
	}

	private SemanticElement findSemanticElementByName(SemanticElementSet semanticElementSet, String semanticElementName) {
		SemanticElement matching = null;
		
		for (SemanticElement other : semanticElementSet.getSemanticElements()) {
			if (semanticElementName.equalsIgnoreCase(other.getName())) {
				matching = other;
				return matching;
			}
		}
		return null;
	}

	// Add the new node to the tree
	private void updateMdmiTree(Node node, SemanticElement se) {

		MdmiModelTree entitySelector = SelectionManager.getInstance().getEntitySelector();
		DefaultTreeModel treeModel = (DefaultTreeModel)entitySelector.getMessageElementsTree().getModel();

		// Node
		Node parentSyntaxNode = node.getParentNode();
		DefaultMutableTreeNode parentTreeNode = entitySelector.findNode(parentSyntaxNode);
		SyntaxNodeNode syntaxTreeNode = null;
		
		if (parentTreeNode instanceof EditableObjectNode) {
			syntaxTreeNode = SyntaxNodeNode.createSyntaxNode(node);
			((EditableObjectNode)parentTreeNode).addSorted(syntaxTreeNode);
			treeModel.nodeStructureChanged(parentTreeNode);
			treeModel.nodeStructureChanged(syntaxTreeNode);
		}
		
		// SE
		SemanticElementSet seSet = se.getElementSet();
		parentTreeNode = entitySelector.findNode(seSet);
		
		if (parentTreeNode instanceof SemanticElementSetNode) {
			SemanticElementSetNode setNode = (SemanticElementSetNode)parentTreeNode;
			setNode.addSorted(new SemanticElementNode(se, setNode.isHierarchical(), true));
			treeModel.nodeStructureChanged(parentTreeNode);
		}
		
		// open the Node
		SelectionManager.getInstance().editItem(syntaxTreeNode);
	}

	// add the child node to the parent and vice versa
	private void addNodeToParent(Node parentNode, Node childNode) {
		childNode.setParentNode(parentNode);
		if (parentNode instanceof Choice) {
			((Choice)parentNode).addNode(childNode);
		} else if (parentNode instanceof Bag){
			((Bag)parentNode).addNode(childNode);
		}
	}

	// generate node(s) for this field
	private void generateChildren(Node parentNode, Field field) {

		String fieldName = field.getName();
		
		if (field.getDatatype() instanceof DTComplex) {
			Bag childNode = new Bag();
			
			childNode.setName(fieldName);
			addNodeToParent(parentNode, childNode);
			
			// repeat for all children
			DTComplex dataType = (DTComplex)field.getDatatype();
			for (Field childField : dataType.getFields()) {
				generateChildren(childNode, childField);
			}
		} else {
			LeafSyntaxTranslator childNode = new LeafSyntaxTranslator();
			childNode.setFieldName(fieldName);
			childNode.setMinOccurs(parentNode.getMinOccurs());
			childNode.setMaxOccurs(parentNode.getMaxOccurs());
			String location = fieldName;
			if (m_attrBox.isSelected()) {
				location = "@" + fieldName;
			}
			childNode.setLocation(location);
			childNode.setFormatExpressionLanguage(m_formatExpressionLanguage.getText().trim());
			
			childNode.setName(fieldName);
			addNodeToParent(parentNode, childNode);
		}
	}
	

	///////////////////////////////////////////
	//  Intermediate Classes
	///////////////////////////////////////////
	private static class ChildNodeWrapper {
		Class<? extends Node> m_theClass;
		public ChildNodeWrapper(Class<? extends Node> clazz) {
			m_theClass = clazz;
		}
		@Override
		public String toString() {
			return ClassUtil.beautifyName(m_theClass);
		}
	}
	
	private static class MdmiDatatypeWrapper {
		MdmiDatatype dataType;
		public MdmiDatatypeWrapper(MdmiDatatype dataType) {
			this.dataType = dataType;
		}
		@Override
		public String toString() {
			return dataType.getTypeName();
		}
	}
	private static class MessageModelWrapper {
		MessageModel messageModel;
		public MessageModelWrapper(MessageModel model) {
			this.messageModel = model;
		}
		@Override
		public String toString() {
			return messageModel.getMessageModelName();
		}
	}

	/** Wrapper for JCheckBox created from a Field */
	public static class FieldCheckBox extends JCheckBox {
		public Field field;
		public FieldCheckBox(Field field) {
			super.setText(field.getName());
			this.field = field;
		}
	}

	
	
	///////////////////////////////////////////
	//  Listener Methods
	///////////////////////////////////////////
	private String oldNodeName = "";
	private void dataEntered(Document doc)
	{
		if (m_nodeName.getDocument() == doc) {
			// fill in SE name, if not populated yet
			String newNodeName = getNodeName();
			String seName = getSemanticElementName();
			if (seName.isEmpty() || seName.equalsIgnoreCase(oldNodeName))
			{
				m_seName.setText(newNodeName);
			}
			oldNodeName = newNodeName;
		}
		setDirty(true);
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		dataEntered(e.getDocument());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		dataEntered(e.getDocument());
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		dataEntered(e.getDocument());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_datatypes) {
			// fill Field list
			fillFieldSelectionList();
		}
		setDirty(true);
	}
	
}
