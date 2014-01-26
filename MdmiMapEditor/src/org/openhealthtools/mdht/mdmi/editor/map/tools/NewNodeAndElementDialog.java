package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.ClassUtil;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.DataFormatException;
import org.openhealthtools.mdht.mdmi.editor.map.editor.IntegerField;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.editor.map.tree.EditableObjectNode;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.editor.map.tree.SemanticElementNode;
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
	private JTextField  m_location  = new JTextField();
	private IntegerField  m_maxOccurs = new IntegerField(8);

	// Semantic Element fields
	private JComboBox<MessageModelWrapper> m_models = new JComboBox<MessageModelWrapper>();
	private JTextField  m_seName  = new JTextField();
	private JComboBox<SemanticElementType> m_seTypes = new JComboBox<SemanticElementType>();
	private JComboBox<Object> m_datatypes = new JComboBox<Object>();
	
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
		// | Max Occurs: [________] [x]Unbounded |
		//  -------------------------------------
		//  - Semantic Element ------------------
		// | Name:       [____________________]  |
		// | Datatype:   [__________________|v]  |
		// | SE Type:    [__________________|v]  |
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
		gbc.weighty = 0;

		// Node Name: 
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.nodeName")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		syntaxNodePanel.add(m_nodeName, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Type: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.nodeType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.insets.left = 0;
		syntaxNodePanel.add(m_nodeTypes, gbc);
		//gbc.insets.left = Standards.LEFT_INSET;

		// Location: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		syntaxNodePanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.location")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		syntaxNodePanel.add(m_location, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
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
		try {
			m_maxOccurs.setDisplayValue("1");
		} catch (DataFormatException e) {
		}
		m_maxOccurs.addUnboundedBox();
		syntaxNodePanel.add(m_maxOccurs, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		return syntaxNodePanel;
	}


	//  - Semantic Element ------------------
	// | Name:       [____________________]  |
	// | Datatype:   [__________________|v]  |
	// | SE Type:    [__________________|v]  |
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
			gbc.insets.left = 0;
			semanticElementPanel.add(m_models, gbc);
			gbc.insets.left = Standards.LEFT_INSET;

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
		gbc.insets.left = 0;
		semanticElementPanel.add(m_seName, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Data Type: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		semanticElementPanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.seType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		semanticElementPanel.add(m_seTypes, gbc);
		gbc.insets.left = Standards.LEFT_INSET;

		// Data Type: 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		semanticElementPanel.add(new JLabel(s_res.getString("NewNodeAndElementDialog.dataType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.left = 0;
		semanticElementPanel.add(m_datatypes, gbc);
		gbc.insets.left = Standards.LEFT_INSET;
		
		return semanticElementPanel;
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
		int maxOccurs = getMaxOccurs();
		MdmiDatatype datatype = getDatatype();
		
		// 1. Create Node
		Node node = null;
		ChildNodeWrapper nodeType = (ChildNodeWrapper)m_nodeTypes.getSelectedItem();
		if (nodeType.m_theClass.equals(Bag.class)) {
			node = new Bag();
		} else if (nodeType.m_theClass.equals(Choice.class)) {
			node = new Choice();
		} else {
			node = new LeafSyntaxTranslator();
		}
		node.setName(getNodeName());
		node.setLocation(m_location.getText().trim());
		node.setMinOccurs(0);
		node.setMaxOccurs(maxOccurs);
		
		addNodeToParent(m_parentNode, node);
		
		////////////////////////////////////////////////
		// 2. Create Semantic Element
		MessageModel msgModel = getMessageModel();
		SemanticElementSet semanticElementSet = msgModel.getElementSet();
		SemanticElement semanticElement = new SemanticElement();
		
		semanticElement.setName(getSemanticElementName());
		semanticElement.setDatatype(datatype);
		semanticElement.setMultipleInstances(maxOccurs > 1);
		
		semanticElementSet.addSemanticElement(semanticElement);
		semanticElement.setElementSet(semanticElementSet);

		////////////////////////////////////////////////
		// 3. Connect SE and Node
		semanticElement.setSyntaxNode(node);
		node.setSemanticElement(semanticElement);
		
		////////////////////////////////////////////////////
		// 4. Generate children
		if (datatype instanceof DTComplex && (node instanceof Bag || node instanceof Choice)) {
			generateChildren(node, (DTComplex)datatype);
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

		} else {
			// TODO
		}
		
		// SE
		SemanticElementSet seSet = se.getElementSet();
		parentTreeNode = entitySelector.findNode(seSet);
		
		if (parentTreeNode instanceof EditableObjectNode) {
			((EditableObjectNode)parentTreeNode).addSorted(new SemanticElementNode(se));
			treeModel.nodeStructureChanged(parentTreeNode);

		} else {
			// TODO
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

	// generate children nodes for all fields in the datatype
	private void generateChildren(Node parentNode, DTComplex complexType) {
		for (Field field : complexType.getFields()) {
			String fieldName = field.getName();
			
			if (field.getDatatype() instanceof DTComplex) {
				Bag childNode = new Bag();
				
				childNode.setName(fieldName);
				addNodeToParent(parentNode, childNode);
				
				// keep going
				generateChildren(childNode, (DTComplex)field.getDatatype());
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

				
				childNode.setName(fieldName);
				addNodeToParent(parentNode, childNode);
			}
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
		setDirty(true);
	}
	
}
