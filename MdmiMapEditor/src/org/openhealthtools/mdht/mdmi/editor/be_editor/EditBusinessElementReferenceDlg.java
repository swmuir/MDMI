package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.UniqueID;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CustomTextArea;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;


/** A dialog for entering or editing MDMIBusinessElementReference */
public class EditBusinessElementReferenceDlg extends BaseDialog {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");

	private MdmiBusinessElementReference m_berModel = null;
	private boolean m_isNew = false;
	
	private int m_cols = 40;
	private JTextField m_name = new JTextField(m_cols);
	private JTextArea  m_description = new CustomTextArea(4, m_cols);
	private JTextField m_url = new JTextField(m_cols); 
	private JTextField m_uid = new JTextField(m_cols);
	//private JTextField m_datatype = new JTextField(m_cols);
	private DataTypeSelector m_datatype = new DataTypeSelector(MdmiDatatype.class);	// all types

	private ActionListener m_actionListener = new BEActionListener();
	private DocumentListener m_textListener = new TextFieldListener();

	public EditBusinessElementReferenceDlg(Dialog owner) {
		this(owner, null);	// new
	}
	public EditBusinessElementReferenceDlg(Frame owner) {
		this(owner, null);	// new
	}

	public EditBusinessElementReferenceDlg(Dialog owner, MdmiBusinessElementReference ber) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);

		construct(ber);
	}
	
	public EditBusinessElementReferenceDlg(Frame owner, MdmiBusinessElementReference ber) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);

		construct(ber);
	}

	private void construct(MdmiBusinessElementReference ber) {
		m_isNew = false;
		
		if (ber == null) {
			// create a new one
			m_isNew = true;
			ber = new MdmiBusinessElementReference();
		}
		m_berModel = ber;
		
		if (m_isNew) {
			setTitle(s_res.getString("EditBusinessElementReferenceDlg.createTitle"));
		} else {
			setTitle(MessageFormat.format(s_res.getString("EditBusinessElementReferenceDlg.modifyTitleFmt"),
					ber.getName()));
		}
		
		if (m_berModel.getUniqueIdentifier() == null || m_berModel.getUniqueIdentifier().isEmpty())
		{
			// give it a GUID
			String guid = UniqueID.getUUID();
			m_berModel.setUniqueIdentifier(guid);
		}
		
		// create the main panel
		createMainPanel();
		
		// size it
		pack();
	}

	/** Create the basic ui */
	private void createMainPanel() {
		// Name:        [______________]
		// Description: [______________]
		// URL:         [______________]
		// UID:         [______________]
		// Datatype:    [______________]
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.WEST;

		// Name
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditBusinessElementReferenceDlg.name")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_name, gbc);
		
		// Description
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditBusinessElementReferenceDlg.description")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JScrollPane scroller = new JScrollPane(m_description);
		mainPanel.add(scroller, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// URL
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditBusinessElementReferenceDlg.url")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_url, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// UID (read only)
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditBusinessElementReferenceDlg.uid")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_uid, gbc);
		m_uid.setEditable(false);
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// Data Type
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("EditBusinessElementReferenceDlg.datatype")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_datatype, gbc);
		
		
		// fill in the GUI from the data
		populateView(m_berModel);
		
		// add listeners
		m_name.getDocument().addDocumentListener(m_textListener);
		m_description.getDocument().addDocumentListener(m_textListener);
		m_url.getDocument().addDocumentListener(m_textListener);
		m_datatype.addActionListener(m_actionListener);

	
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
	// model to view
	private void populateView(MdmiBusinessElementReference ref)
	{
		m_name.setText(m_berModel.getName() == null ? "" : m_berModel.getName());
		m_description.setText(m_berModel.getDescription() == null ? "" : m_berModel.getDescription());
		m_url.setText(m_berModel.getReference() == null ? "" : m_berModel.getReference().toString());
		m_uid.setText(m_berModel.getUniqueIdentifier() == null ? "" : m_berModel.getUniqueIdentifier());
		m_datatype.setDataType(m_berModel.getReferenceDatatype());
	}
	
	// view to model
	private boolean populateModel()
	{
		String name = m_name.getText().trim();
		String description = m_description.getText().trim();
		String uriString = m_url.getText().trim();
		String uid = m_uid.getText().trim();
		
		// validate URL
		
		if ((uriString == null || uriString.length() == 0)) {
			String message = "The URI must be filled in";
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		URI uriValue = null;
		try {
			uriValue = URI.create(uriString);
		} catch (IllegalArgumentException ex) {
			m_url.selectAll();
			m_url.requestFocus();
			String message = "The text '" + uriString + "' is not a valid URI";
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		// validate Datatype
		MdmiDatatype datatype = m_datatype.getDataType();
		
		if (datatype == null) {
			m_datatype.requestFocus();
			String message = "The datatype does not exist";
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		// everything is good
		m_berModel.setName(name);
		m_berModel.setDescription(description);
		m_berModel.setReference(uriValue);
		m_berModel.setReferenceDatatype(datatype);
		m_berModel.setUniqueIdentifier(uid);
		
		return true;
	}
	
	// get the model
	public MdmiBusinessElementReference getBusinessElementReference() {
		return m_berModel;
	}
	
	// is it  new
	public boolean isNew() {
		return m_isNew;
	}

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_name.getDocument().removeDocumentListener(m_textListener);
		m_description.getDocument().removeDocumentListener(m_textListener);
		m_url.getDocument().removeDocumentListener(m_textListener);
		m_datatype.removeActionListener(m_actionListener);
	}

	@Override
	public boolean isDataValid() {
		// Name,  UID,  Data Type required
		String name  = m_name.getText().trim();
		String UID  = m_uid.getText().trim();
		MdmiDatatype dataType  = m_datatype.getDataType();
		return (!name.isEmpty() && !UID.isEmpty() && dataType != null);
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
	
	private class BEActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			setDirty(true);
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
