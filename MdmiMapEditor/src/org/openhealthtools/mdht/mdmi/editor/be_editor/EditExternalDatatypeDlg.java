package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.net.URI;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openhealthtools.mdht.mdmi.model.DTExternal;


/** A dialog for entering or editing External datatypes (DTExternal) */
public class EditExternalDatatypeDlg extends EditDatatypeDlg {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	protected JTextField m_typeSpec = new JTextField(m_cols);


	public EditExternalDatatypeDlg(Dialog owner, DTExternal datatype) {
		super(owner, datatype);
		createMainPanel();
	}
	
	public EditExternalDatatypeDlg(Frame owner, DTExternal datatype) {
		super(owner, datatype);
		createMainPanel();
	}

	

	/** Create the basic ui */
	@Override
	protected JPanel createMainPanel() {
		// Name        [_______]
		// Description [_______]
		// Type Spec   [_______]
		
		JPanel mainPanel = super.createMainPanel();	// Name and Description fields

		GridBagConstraints gbc = m_gbc;

		// Type Spec
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditExternalDatatypeDlg.typeSpec")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_typeSpec, gbc);


		// fill in the GUI from the data
		populateView();
		
		// add listeners
		m_typeSpec.getDocument().addDocumentListener(m_textListener);
		
		// size it
		pack(new Dimension(550,200));
		
		return mainPanel;
		
	}
	
	
	// model to view
	@Override
	public void populateView()
	{
		if (m_datatypeModel == null) {
			return;
		}

		URI uri = getExternalDatatype().getTypeSpec();
		m_typeSpec.setText( uri == null ? "" : uri.toString());

		super.populateView();
		
		
	}
	
	// view to model
	@Override
	public boolean populateModel()
	{
		// everything is good
		// create one if we need to
		if (m_datatypeModel == null) {
			m_datatypeModel = new DTExternal();
			// we'll leave the owner null, so we know it's new
			//m_datatypeModel.setOwner(ServerInterface.getInstance().getMessageGroup());
		}

		// validate URI
		String typeSpecText = m_typeSpec.getText().trim();
		URI typeSpecURI = null;
		try {
			typeSpecURI = (typeSpecText == null || typeSpecText.length() == 0) ? null :
				URI.create(typeSpecText);
		} catch (IllegalArgumentException ex) {
			m_typeSpec.selectAll();
			m_typeSpec.requestFocus();
			String message = "The text '" + typeSpecText + "' is not a valid URI";
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		getExternalDatatype().setTypeSpec(typeSpecURI);
		
		if (!super.populateModel()) {
			return false;
		}
		
		
		return true;
	}
	
	
	// get the model
	public DTExternal getExternalDatatype() {
		return (DTExternal)getDatatype();
	}
	

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_typeSpec.getDocument().removeDocumentListener(m_textListener);
	}
	
}
