package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.Mdmi;
import org.openhealthtools.mdht.mdmi.MdmiResolver;
import org.openhealthtools.mdht.mdmi.MdmiValueSet;
import org.openhealthtools.mdht.mdmi.MdmiValueSetMap;
import org.openhealthtools.mdht.mdmi.MdmiValueSetsHandler;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.SelectionManager;
import org.openhealthtools.mdht.mdmi.editor.map.editor.MdmiDatatypeField;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;

/** A dialog to identify the From and To attributes for value set mapping.
 * The attributes can be either a Datatype, or a CSV File (in the form Code, Name, (Description))
 * @author Sally Conway
 *
 */
public class ValueSetIdentifierDialog extends BaseDialog {
	private JComboBox<MessageGroup> m_messageGroupSelector  = new JComboBox<MessageGroup>();
	
	private JComboBox<String> m_srcValueSetSelector = new JComboBox<String>();
	private JComboBox<String> m_targetValueSetSelector = new JComboBox<String>();
	

	// Listener for data changes
	private DataChangedListener m_listener = new DataChangedListener();

	
	// Handler
	private MdmiValueSetsHandler m_handler = null;
	
	private ModelRenderers.MessageGroupRenderer m_messageGroupRenderer = new ModelRenderers.MessageGroupRenderer();

	public ValueSetIdentifierDialog(Frame owner) {
		super(owner);
		setTitle("Create Value Set Mapping");

		
		buildUI();
		pack(new Dimension(500, 100));
	}

	//  Create a new value set mapping from two value sets

	//      Message Group: [__________________|v]
	//      From Value Set:      [__________________|v] 
	//      To Value Set:        [__________________|v] 
	//
	private void buildUI() {
		JPanel mainPanel = new JPanel(new GridBagLayout());
		add(mainPanel, BorderLayout.CENTER);
		
		// allow editing of value set names
		m_srcValueSetSelector.setEditable(true);
		m_targetValueSetSelector.setEditable(true);

		List<MessageGroup> messageGroups = SelectionManager.getInstance().getEntitySelector().getMessageGroups();
		for (MessageGroup group : messageGroups) {
			m_messageGroupSelector.addItem(group);
		}
		if (messageGroups.size() == 1) {
			m_messageGroupSelector.setEnabled(false);
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		

		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		JLabel textMessage = new JLabel("Create a new Mapping between two Value Sets");
		mainPanel.add(textMessage, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;

		
		// Message Group
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel msgGroupLabel = new JLabel("Message Group:");
		mainPanel.add(msgGroupLabel, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_messageGroupSelector, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// From Value Set
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("From Value Set:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_srcValueSetSelector, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		
		// To Value Set
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("To Value Set:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_targetValueSetSelector, gbc);
		
		// fill data types from first group
		fillDataTypes();

		// add listeners
		m_messageGroupSelector.addActionListener(m_listener);
		m_messageGroupSelector.setRenderer(m_messageGroupRenderer);
		((JTextField)m_srcValueSetSelector.getEditor().getEditorComponent()).getDocument().addDocumentListener(m_listener);
		((JTextField)m_targetValueSetSelector.getEditor().getEditorComponent()).getDocument().addDocumentListener(m_listener);
	}


	/** Fill in the data type selector using the selected Message Group */
	private void fillDataTypes() {
		MessageGroup group = (MessageGroup)m_messageGroupSelector.getSelectedItem();
		
		MdmiResolver resolver = Mdmi.INSTANCE.getResolver();
		if (resolver != null) {
			m_handler = resolver.getValueSetsHandler(group.getName());
		}
		// testing
		if (m_handler == null) {
			m_handler = createDummyHandler();
		}
		
		List<MdmiDatatype> types = new ArrayList<MdmiDatatype>();
		types.addAll(group.getDatatypes());
		// Sort by type name
		Collections.sort(types, MdmiDatatypeField.getDatatypeComparator());

		// remove action listeners while we're re-populating the selectors
		m_srcValueSetSelector.removeActionListener(m_listener);
		m_targetValueSetSelector.removeActionListener(m_listener);
		
		m_srcValueSetSelector.removeAllItems();
		m_targetValueSetSelector.removeAllItems();

		m_srcValueSetSelector.addItem("");	// make first entry blank
		m_targetValueSetSelector.addItem("");	// make first entry blank
		
		if (m_handler != null) {
			for (MdmiValueSet valueSet : m_handler.getAllValueSets()) {
				m_srcValueSetSelector.addItem(valueSet.getName());
				m_targetValueSetSelector.addItem(valueSet.getName());
			}
		}

		// put back the action listeners
		m_srcValueSetSelector.addActionListener(m_listener);
		m_targetValueSetSelector.addActionListener(m_listener);
	}
	


	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_messageGroupSelector.removeActionListener(m_listener);
		m_messageGroupSelector.setRenderer(null);
		
		m_srcValueSetSelector.removeActionListener(m_listener);
		((JTextField)m_srcValueSetSelector.getEditor().getEditorComponent()).getDocument().removeDocumentListener(m_listener);
		m_targetValueSetSelector.removeActionListener(m_listener);
		((JTextField)m_targetValueSetSelector.getEditor().getEditorComponent()).getDocument().removeDocumentListener(m_listener);
	}

	
	
	@Override
	public boolean isDataValid() {
		// Value Set Names must be defined, and different
		String fromValueSetName = getSrcValueSetName();
		String toValueSetName = getTargetValueSetName();
		return (!fromValueSetName.isEmpty() && !toValueSetName.isEmpty() &&
				!fromValueSetName.equalsIgnoreCase(toValueSetName));
	}


	@Override
	protected void okButtonAction() {
		String srcValueSetName = getSrcValueSetName();
		String targetValueSetName = getTargetValueSetName();

		
		// create bi-directional ValueSetEditor dialog
		ValueSetMapEditor valueSetEditor = new ValueSetMapEditor(this, true,
				m_handler, srcValueSetName, targetValueSetName);

		// show it
		valueSetEditor.center();
		int val = valueSetEditor.display();
		if (val == OK_BUTTON_OPTION) {
			super.okButtonAction();
		}
	}
	
	public String getSrcValueSetName() {
		JTextField editField = (JTextField)m_srcValueSetSelector.getEditor().getEditorComponent();
		String value = editField.getText();
		return value.trim();
	}
	
	public String getTargetValueSetName() {
		JTextField editField = (JTextField)m_targetValueSetSelector.getEditor().getEditorComponent();
		String value = editField.getText();
		return value.trim();
	}


	private class DataChangedListener implements ActionListener, DocumentListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == m_messageGroupSelector) {
				fillDataTypes();
				setDirty(true);
			}
			setDirty(true);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			setDirty(true);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			setDirty(true);
		}
		
	}
	
	///////////////////////////////////////////
	// TESTING
	///////////////////////////////////////////
	public static MdmiValueSetsHandler createDummyHandler() {

		String filename = "MdmiMapEditor.valuesets.xml";
		File f = new File(filename);
		

		MdmiValueSetsHandler vsh;
		if (f.exists()) {
			vsh = new MdmiValueSetsHandler(f);
		} else {
			vsh = new MdmiValueSetsHandler(null);

			MdmiValueSet vs1 = new MdmiValueSet(vsh, "Colors");
			vs1.addValue("RED", "The color red");
			vs1.addValue("GREEN", "The color green");
			vs1.addValue("BLUE", "the color blue");
			vs1.addValue("WHITE", "The color white");
			vs1.addValue("PEACH", null);
			vs1.addValue("BEIGE", "The color beige");
			vsh.addValueSet(vs1);
			
			MdmiValueSet vs2 = new MdmiValueSet(vsh, "ColorCodes");
			vs2.addValue("R", "Red abbreviation");
			vs2.addValue("B", "Blue abbreviation");
			vs2.addValue("G", "Green abbreviation");
			vs2.addValue("W", "White abbreviation");
			vsh.addValueSet(vs2);
			
			MdmiValueSetMap vsm1 = new MdmiValueSetMap(vsh, vs1, vs2);
			vsm1.addMapping("RED", "R");
			vsm1.addMapping("BLUE", "B");
			vsm1.addMapping("GREEN", "G");
			vsm1.addMapping("WHITE", "W");
			vsm1.addMapping("PEACH", "W");
			vsh.addValueSetMap(vsm1);
			
			MdmiValueSetMap vsm2 = new MdmiValueSetMap(vsh, vs2, vs1);
			vsm2.addMapping("R", "RED");
			vsm2.addMapping("B", "BLUE");
			vsm2.addMapping("G", "GREEN");
			vsm2.addMapping("W", "BEIGE");
			vsh.addValueSetMap(vsm2);

			// force it to exist
			vsh.save(f);
		}

		return vsh;
	}

}
