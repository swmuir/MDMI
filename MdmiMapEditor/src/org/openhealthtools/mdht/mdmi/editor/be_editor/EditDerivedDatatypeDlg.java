package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.DTSimple;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;


/** A dialog for entering or editing Derived datatypes (DTSDerived) */
public class EditDerivedDatatypeDlg extends EditDatatypeDlg {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	protected JTextField m_restriction = new JTextField(m_cols);
	protected DataTypeSelector m_baseType = new DataTypeSelector(DTSimple.class);	// only simple types


	public EditDerivedDatatypeDlg(Dialog owner, DTSDerived datatype) {
		super(owner, datatype);
		createMainPanel();
	}
	
	public EditDerivedDatatypeDlg(Frame owner, DTSDerived datatype) {
		super(owner, datatype);
		createMainPanel();
	}

	

	/** Create the basic ui */
	@Override
	protected JPanel createMainPanel() {
		// Name        [_______]
		// Description [_______]
		// Restriction [_______]
		// Base Type   [______v]
		
		JPanel mainPanel = super.createMainPanel();	// Name and Description fields

		GridBagConstraints gbc = m_gbc;

		// Restriction
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditDerivedDatatypeDlg.restriction")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_restriction, gbc);

		// Bases Type
		// don't allow base type to be this object
		if (m_datatypeModel != null) {
			m_baseType.excludeDatatype(m_datatypeModel);
		}
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditDerivedDatatypeDlg.baseType")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_baseType, gbc);

		// fill in the GUI from the data
		populateView();
		
		// add listeners
		m_restriction.getDocument().addDocumentListener(m_textListener);
		m_baseType.addActionListener(m_actionListener);
		
		// size it
		pack(new Dimension(500,300));
		
		return mainPanel;
		
	}
	
	
	// model to view
	@Override
	public void populateView()
	{
		if (m_datatypeModel == null) {
			return;
		}

		m_restriction.setText(getDerivedDatatype().getRestriction());
		m_baseType.setDataType(getDerivedDatatype().getBaseType());

		super.populateView();
		
		
	}
	
	// view to model
	@Override
	public boolean populateModel()
	{
		// everything is good
		// create one if we need to
		if (m_datatypeModel == null) {
			m_datatypeModel = new DTSDerived();
			// we'll leave the owner null, so we know it's new
			//m_datatypeModel.setOwner(ServerInterface.getInstance().getMessageGroup());
		}

		String restriction = m_restriction.getText().trim();
		MdmiDatatype baseType = m_baseType.getDataType();

		// validate Base Type if required
		if (baseType == null) {
			m_baseType.requestFocus();
			String message = "A Base Type is required";
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		getDerivedDatatype().setRestriction(restriction);
		getDerivedDatatype().setBaseType((DTSimple)baseType);
		
		if (!super.populateModel()) {
			return false;
		}
		
		
		return true;
	}
	
	
	// get the model
	public DTSDerived getDerivedDatatype() {
		return (DTSDerived)getDatatype();
	}
	

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_restriction.getDocument().removeDocumentListener(m_textListener);
		m_baseType.removeActionListener(m_actionListener);
	}
	
}
