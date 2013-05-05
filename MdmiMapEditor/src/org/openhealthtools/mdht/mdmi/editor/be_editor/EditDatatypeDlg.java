package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CustomTextArea;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;


/** An abstract dialog for entering or editing basic datatypes.
 * Derived classes must call createMainPanel() and modelToView() to populate the main panel of the dialog. */
public class EditDatatypeDlg extends BaseDialog {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	protected MdmiDatatype m_datatypeModel = null;
	protected boolean m_isNew = false;
	
	protected int m_cols = 40;
	protected JTextField m_name = new JTextField(m_cols);
	protected JTextArea  m_description = new CustomTextArea(4, m_cols);
	
	protected DocumentListener m_textListener = new TextFieldListener();
	protected GridBagConstraints m_gbc = new GridBagConstraints();

	public EditDatatypeDlg(Dialog owner) {
		this(owner, null);	// new
	}
	public EditDatatypeDlg(Frame owner) {
		this(owner, null);	// new
	}

	public EditDatatypeDlg(Dialog owner, DTComplex datatype) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);

		construct(datatype);
	}
	
	public EditDatatypeDlg(Frame owner, DTComplex datatype) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);

		construct(datatype);
	}

	// called by all constructors
	private void construct(MdmiDatatype datatype) {
		m_isNew = false;
		
		if (datatype == null) {
			m_isNew = true;
		}
		m_datatypeModel = datatype;
		
		if (m_isNew) {
			setTitle(s_res.getString("EditDatatypeDlg.createTitle"));
		} else {
			setTitle(MessageFormat.format(s_res.getString("EditDatatypeDlg.modifyTitleFmt"),
					datatype.getName()));
		}
	}

	/** Creates the basic ui */
	protected JPanel createMainPanel() {
		// Name        [_______]
		// Description [_______]
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = m_gbc;
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;

		// Name
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditDatatypeDlg.name")), gbc);
		gbc.gridx++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_name, gbc);
		
		// Description
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditDatatypeDlg.description")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JScrollPane scroller = new JScrollPane(m_description);
		mainPanel.add(scroller, gbc);

		
		m_name.getDocument().addDocumentListener(m_textListener);
		m_description.getDocument().addDocumentListener(m_textListener);
	
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		// return it
		return mainPanel;
	}
	
	
	// model to view
	public void populateView()
	{
		if (m_datatypeModel == null) {
			return;
		}
		m_name.setText(m_datatypeModel.getName() == null ? "" : m_datatypeModel.getName());
		m_description.setText(m_datatypeModel.getDescription() == null ? "" : m_datatypeModel.getDescription());
		
	}
	
	// view to model
	public boolean populateModel()
	{
		String name = m_name.getText().trim();
		String description = m_description.getText().trim();
		
		m_datatypeModel.setTypeName(name);
		m_datatypeModel.setDescription(description);
		
		return true;
	}
	
	
	// get the model
	public MdmiDatatype getDatatype() {
		return m_datatypeModel;
	}
	
	// is it a new datatype
	public boolean isNew() {
		return m_isNew;
	}

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_name.getDocument().removeDocumentListener(m_textListener);
		m_description.getDocument().removeDocumentListener(m_textListener);
	}

	@Override
	public boolean isDataValid() {
		// Name required
		String name  = m_name.getText().trim();
		return (!name.isEmpty());
	}

	@Override
	protected void applyButtonAction() {
		if (populateModel()) {
			super.applyButtonAction();
		}
	}

	@Override
	protected void okButtonAction() {
		if (populateModel()) {
			super.okButtonAction();
		}
	}

	private class TextFieldListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			documentChanged(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			documentChanged(e);
		}
		
		private void documentChanged(DocumentEvent e) {
			setDirty(true);
		}
		
	}
	
}
