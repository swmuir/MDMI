package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.EnumLiteralTable;
import org.openhealthtools.mdht.mdmi.model.DTSEnumerated;


/** A dialog for entering or editing External datatypes (DTSEnumerated) */
public class EditEnumDatatypeDlg extends EditDatatypeDlg {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	protected EnumLiteralTable m_table;


	public EditEnumDatatypeDlg(Dialog owner, DTSEnumerated datatype) {
		super(owner, datatype);
		createMainPanel();
	}
	
	public EditEnumDatatypeDlg(Frame owner, DTSEnumerated datatype) {
		super(owner, datatype);
		createMainPanel();
	}

	

	/** Create the basic ui */
	@Override
	protected JPanel createMainPanel() {
		// Name        [_______]
		// Description [_______]
		//  -- Literals ---------------
		// | Name | Code | Description |
		// |------|------|-------------|
		// |______|______|_____________|
		// |______|______|_____________|
		// |______|______|_____________|
		// |______|______|_____________|
		
		JPanel mainPanel = super.createMainPanel();	// Name and Description fields
		m_table = new EnumLiteralTable();

		GridBagConstraints gbc = m_gbc;

		// Literals
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		JPanel literalsPanel = new JPanel(new BorderLayout());
		JScrollPane scroller = new JScrollPane(m_table);
		literalsPanel.add(scroller, BorderLayout.CENTER);
		Border insideBorder = BorderFactory.createEtchedBorder();
		Border outsideBorder = BorderFactory.createTitledBorder(insideBorder, 
					s_res.getString("EditEnumDatatypeDlg.literals"));
		literalsPanel.setBorder(outsideBorder);
		mainPanel.add(literalsPanel, gbc);


		// fill in the GUI from the data
		populateView();
		

		// size it
		pack(new Dimension(500, 330));
		return mainPanel;
		
	}
	
	
	// model to view
	@Override
	public void populateView()
	{
		if (m_datatypeModel == null) {
			return;
		}
		
		// populate table
		m_table.initialize(getEnumeratedDatatype());

		super.populateView();
		
		
	}
	
	// view to model
	@Override
	public boolean populateModel()
	{
		// everything is good
		// create one if we need to
		if (m_datatypeModel == null) {
			m_datatypeModel = new DTSEnumerated();
			// we'll leave the owner null, so we know it's new
			//m_datatypeModel.setOwner(ServerInterface.getInstance().getMessageGroup());
		}

		// validate Literals
		if (!m_table.populateModel(getEnumeratedDatatype()))
		{
			return false;
		}
		
		if (!super.populateModel()) {
			return false;
		}
		
		
		return true;
	}
	
	
	// get the model
	public DTSEnumerated getEnumeratedDatatype() {
		return (DTSEnumerated)getDatatype();
	}
	

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		//m_typeSpec.getDocument().removeDocumentListener(m_textListener);
	}
	
}
