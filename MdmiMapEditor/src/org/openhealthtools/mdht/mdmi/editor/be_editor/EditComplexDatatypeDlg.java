package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.VerticalButtonPanel;
import org.openhealthtools.mdht.mdmi.model.DTCChoice;
import org.openhealthtools.mdht.mdmi.model.DTCStructured;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.Field;


/** A dialog for entering or editing Complex datatypes (DTCChoice or DTCStructured) */
public class EditComplexDatatypeDlg extends EditDatatypeDlg {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	private JRadioButton m_structuredBtn = new JRadioButton("Structured", true);
	private JRadioButton m_choiceBtn = new JRadioButton("Choice", false);

	
	private ImageIcon addIcon;
	private JButton m_addFieldButton = null;
	private JButton m_removeFieldButton = null;
	private JTabbedPane m_fieldsTabPane = new JTabbedPane();
	
	// Fields
	private ArrayList<DatabaseFieldPanel> m_fieldPanels = new ArrayList<DatabaseFieldPanel>();
	
	private ActionListener m_typeListener = new DataTypeTypeListener();


	public EditComplexDatatypeDlg(Dialog owner, DTComplex datatype) {
		super(owner, datatype);
		createMainPanel();
	}
	
	public EditComplexDatatypeDlg(Frame owner, DTComplex datatype) {
		super(owner, datatype);
		createMainPanel();
	}

	

	/** Create the basic ui */
	@Override
	protected JPanel createMainPanel() {
		// Name        [_______]
		// Description [_______]
		// Type (o)Structured  (  )Choice
		//  ---Fields-------------------
		// |                            |
		// |                     [New]  |
		// |                     {Del]  |
		// |                            |
		// |                            |
		//  ----------------------------

		// create icon buttons
		URL imageURL = getClass().getResource(s_res.getString("AddRemoveFieldPanel.addIcon"));
		if (imageURL != null) {
			ImageIcon icon =  new ImageIcon(imageURL);
			m_addFieldButton = new JButton("New", icon);
		}
		imageURL = getClass().getResource(s_res.getString("AddRemoveFieldPanel.removeIcon"));
		if (imageURL != null) {
			ImageIcon icon =  new ImageIcon(imageURL);
			m_removeFieldButton = new JButton("Delete", icon);
		}
		
		JPanel mainPanel = super.createMainPanel();	// Name and Description fields

		GridBagConstraints gbc = m_gbc;
		
		gbc.gridx = 0;
		gbc.gridy++;

		// Choice vs Structured
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));
		ButtonGroup group = new ButtonGroup();
		group.add(m_structuredBtn);
		group.add(m_choiceBtn);
		buttonPanel.add(new JLabel(s_res.getString("EditComplexDatatypeDlg.type")));
		buttonPanel.add(m_structuredBtn);
		buttonPanel.add(m_choiceBtn);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		mainPanel.add(buttonPanel, gbc);
		
		// Fields
		JPanel fieldPanel = new JPanel(new BorderLayout(Standards.LEFT_INSET, Standards.TOP_INSET)); 
		VerticalButtonPanel fieldButtons = new VerticalButtonPanel();
		// [+ New] [X Delete]
		fieldButtons.addStrut(36);
		fieldButtons.add(m_addFieldButton);
		fieldButtons.add(m_removeFieldButton);
		fieldPanel.add(fieldButtons, BorderLayout.EAST);

		fieldPanel.add(m_fieldsTabPane, BorderLayout.CENTER);
		Border insideBorder = BorderFactory.createEtchedBorder();
		Border outsideBorder = BorderFactory.createTitledBorder(insideBorder, 
					s_res.getString("EditComplexDatatypeDlg.fields"));
		fieldPanel.setBorder(outsideBorder);

		// Add field panel to main
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		
		mainPanel.add(fieldPanel, gbc);
		
		// add FieldPanels to the tab
		int numFieldPanels = 0;
		if (m_datatypeModel != null) {
			numFieldPanels = getComplexDatatype().getFields().size();
		}
		for (int i=0; i<numFieldPanels; i++) {
			addNewFieldPanel();
		}

		// set focus on first tab
		if (numFieldPanels > 0) {
			m_fieldsTabPane.setSelectedIndex(0);
		} else {
			m_removeFieldButton.setEnabled(false);
			// set minimum size of tab
			Dimension size1 = new Dimension(800, 205);
			m_fieldsTabPane.setMinimumSize(size1);
		}
		

		// fill in the GUI from the data
		populateView();
		
		// add listeners
		m_structuredBtn.addActionListener(m_typeListener);
		m_choiceBtn.addActionListener(m_typeListener);
		m_addFieldButton.addActionListener(m_typeListener);
		m_removeFieldButton.addActionListener(m_typeListener);
		
		// size it
		pack();
		
		return mainPanel;
		
	}
	
	// add a new DatabaseFieldPanel to the display
	private void addNewFieldPanel() {
		DatabaseFieldPanel fieldPanel = new DatabaseFieldPanel();
		m_fieldPanels.add(fieldPanel);


		// add to tabs
		int idx = m_fieldPanels.size();
		m_fieldsTabPane.addTab("Field "+idx, addIcon, fieldPanel);
		
		// set focus
		m_fieldsTabPane.setSelectedIndex(idx-1);
	}
	
	// model to view
	@Override
	public void populateView()
	{
		if (m_datatypeModel == null) {
			return;
		}


		super.populateView();
		
		
		if (m_datatypeModel instanceof DTCChoice) {
			m_choiceBtn.setSelected(true);
			m_structuredBtn.setSelected(false);
		} else {
			m_choiceBtn.setSelected(false);
			m_structuredBtn.setSelected(true);
		}
		
		// if it exists, we can't change the type
		if (m_datatypeModel.getOwner() != null) {
			m_choiceBtn.setEnabled(false);
			m_structuredBtn.setEnabled(false);
		}
		
		// fill in fields
		ArrayList<Field> fieldsInDatatype = getComplexDatatype().getFields();
		for (int i=0; i<fieldsInDatatype.size(); i++) {
			Field field = fieldsInDatatype.get(i);
			
			DatabaseFieldPanel panel = m_fieldPanels.get(i);
			if (panel != null) {
				panel.populateView(field);
			}
			
			// update name
			if (field.getName() != null) {
				m_fieldsTabPane.setTitleAt(i, field.getName());
			}
		}
		
	}
	
	// view to model
	@Override
	public boolean populateModel()
	{
		// everything is good
		// create one if we need to
		if (m_datatypeModel == null) {
			if (m_choiceBtn.isSelected()) {
				m_datatypeModel = new DTCChoice();
			} else {
				m_datatypeModel = new DTCStructured();
			}
			// we'll leave the owner null, so we know it's new
			//m_datatypeModel.setOwner(ServerInterface.getInstance().getMessageGroup());
		}
		
		if (!super.populateModel()) {
			return false;
		}
		
		// fill in fields
		ArrayList<Field> fields = getComplexDatatype().getFields();
		if (fields == null) {
			fields = new ArrayList<Field>();
			getComplexDatatype().setFields(fields);
		} else {
			// delete any fields that don't have a panel
			for (int i=fields.size()-1; i>=0; i--) {
				Field field = fields.get(i);
				// find panel
				DatabaseFieldPanel fieldPanel = null;
				for (int t=0; t<m_fieldPanels.size(); t++) {
					DatabaseFieldPanel panel = m_fieldPanels.get(t);
					if (panel.getField() == field) {
						fieldPanel = panel;
						break;
					}
				}
				if (fieldPanel == null) {
					// delete this one
					fields.remove(i);
					m_fieldsTabPane.removeTabAt(i);
				}
			}
		}
			
		// Add or Update Field
		for (int i=0; i<m_fieldPanels.size(); i++) {
			DatabaseFieldPanel panel = m_fieldPanels.get(i);
			Field field = panel.getField();
			if (field == null) {
				// new
				field = new Field();
				field.setOwnerType(getComplexDatatype());
				fields.add(field);
			}
			// update
			if (!panel.populateModel(field)) {
				// populate failed - show panel
				m_fieldsTabPane.setSelectedIndex(i);
				return false;
			}
		}
		
		
		return true;
	}
	
	
	// get the model
	public DTComplex getComplexDatatype() {
		return (DTComplex)getDatatype();
	}
	

	@Override
	public void dispose() {
		super.dispose();
		m_fieldPanels.clear();
		
		// remove all listeners
		m_structuredBtn.removeActionListener(m_typeListener);
		m_choiceBtn.removeActionListener(m_typeListener);
		m_addFieldButton.removeActionListener(m_typeListener);
		m_removeFieldButton.removeActionListener(m_typeListener);
	}

	
	private class DataTypeTypeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// some components may need to be enabled/disabled/hidden
			if (e.getSource() == m_addFieldButton) {
				// add a new tab
				addNewFieldPanel();
				
				// resize if necessary
				Dimension size = getSize();
				Dimension prefSize = getPreferredSize();

				if (size.width < prefSize.width || size.height < prefSize.height) {
					pack();
				}
				
			} else if (e.getSource() == m_removeFieldButton) {
				// delete selected panel
				int idx = m_fieldsTabPane.getSelectedIndex();
				if (idx != -1) {
					m_fieldsTabPane.removeTabAt(idx);
					m_fieldPanels.remove(idx);
				}
			}
			
			// don't allow removal if there are no fields
			m_removeFieldButton.setEnabled (m_fieldsTabPane.getTabCount() > 0);
			setDirty(true);
		}
	}

	
}
