package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.openhealthtools.mdht.mdmi.editor.common.IntegerDocumentFilter;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.CustomTextArea;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;

public class DatabaseFieldPanel extends JPanel {

	// Name:        [______________________________]
	// Description: [______________________________]
	// Min:  [_____]     Max:  [_____] [] Unbounded
	// Datatype:    [______________________________]
	
	private Field m_fieldModel = null;
	
	private int m_cols = 10;
	private JTextField m_name = new JTextField(m_cols);
	private JTextArea  m_description = new CustomTextArea(4, m_cols);
	private JTextField m_min = new JTextField(6); 
	private JTextField m_max = new JTextField(6);
	private JCheckBox  m_unbounded = new JCheckBox("Unbounded", true);
	//private JTextField m_datatype = new JTextField(m_cols);
	private DataTypeSelector m_datatype = new DataTypeSelector();

	private IntegerDocumentFilter m_integerFilter = new IntegerDocumentFilter();
	private DocumentListener m_textListener = new TextFieldListener();
	private ActionListener m_unboundedListener = new UnboundedListener();

	/** Create the basic ui */
	public  DatabaseFieldPanel() {
		// Name:        [______________________________]
		// Description: [______________________________]
		// Min:  [_____]     Max:  [_____] [] Unbounded
		// Datatype:    [______________________________]
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;

		// Name
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		add(new JLabel("Name:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(m_name, gbc);
		
		// Description
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		add(new JLabel("Description:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JScrollPane scroller = new JScrollPane(m_description);
		add(scroller, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// Min/Max
		m_min.setText("0");
		m_max.setText("1");
		JPanel minMaxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));
		minMaxPanel.add(new JLabel("Min Occurs:"));
		minMaxPanel.add(m_min);
		minMaxPanel.add(new JLabel("    "));	// padding
		minMaxPanel.add(new JLabel("Max Occurs:"));
		minMaxPanel.add(m_max);
		minMaxPanel.add(m_unbounded);
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(minMaxPanel, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// Data Type
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		add(new JLabel("Datatype:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		add(m_datatype, gbc);

		// Add a filter to min and max
		((AbstractDocument)m_min.getDocument()).setDocumentFilter(m_integerFilter);
		((AbstractDocument)m_max.getDocument()).setDocumentFilter(m_integerFilter);
		
	}
	
	
	
	@Override
	public void addNotify() {
		super.addNotify();
		
		// Add listeners
		m_name.getDocument().addDocumentListener(m_textListener);
		m_description.getDocument().addDocumentListener(m_textListener);
		m_datatype.addActionListener(m_unboundedListener);
		m_min.getDocument().addDocumentListener(m_textListener);
		m_max.getDocument().addDocumentListener(m_textListener);
		m_unbounded.addActionListener(m_unboundedListener);
	}



	@Override
	public void removeNotify() {
		// Remove listeners
		m_name.getDocument().removeDocumentListener(m_textListener);
		m_description.getDocument().removeDocumentListener(m_textListener);
		m_datatype.removeActionListener(m_unboundedListener);
		m_min.getDocument().removeDocumentListener(m_textListener);
		m_max.getDocument().removeDocumentListener(m_textListener);
		m_unbounded.removeActionListener(m_unboundedListener);
		
		super.removeNotify();
	}

	// do we have a field
	public Field getField() {
		return m_fieldModel;
	}

	// model to view
	public void populateView(Field field)
	{
		m_fieldModel = field;
		
		m_name.setText(field.getName() == null ? "" : field.getName());
		m_description.setText(field.getDescription() == null ? "" : field.getDescription());
		m_min.setText(String.valueOf( field.getMinOccurs() ));
		m_max.setText(String.valueOf( field.getMaxOccurs() ));
		if (field.getMaxOccurs() == Integer.MAX_VALUE) {
			m_max.setText("");
			m_max.setEditable(false);
			m_unbounded.setSelected(true);
		}
		m_datatype.setDataType(field.getDatatype());
	}
	
	// view to model
	public boolean populateModel(Field field)
	{
		String name = m_name.getText().trim();
		String description = m_description.getText().trim();

		if (name.isEmpty()) {
			m_name.selectAll();
			m_name.requestFocus();
			String message = "The field must have a name";
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

		// Check Min and Max (filter should handle validation)
		int minValue = 0;
		if (!m_min.getText().trim().isEmpty()) {
			minValue = Integer.parseInt(m_min.getText().trim());
		}
		int maxValue = Integer.MAX_VALUE;
		if (!m_unbounded.isSelected()) {
			maxValue = Integer.parseInt(m_max.getText().trim());
		}
		if (minValue > maxValue) {
			m_min.selectAll();
			m_min.requestFocus();
			String message = "The Min value may not exceede the Max value";
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		// everything is good
		field.setName(name);
		field.setDescription(description);
		field.setDatatype(datatype);
		field.setMinOccurs(minValue);
		field.setMaxOccurs(maxValue);
		
		return true;
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
			//setDirty(true);
		}
	}
	
	private class UnboundedListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == m_unbounded) {
				if (m_unbounded.isSelected()) {
					m_max.setEditable(false);
				} else {
					m_max.setEditable(true);
				}
			} else {
				//setDirty(true);
			}
		}
		
	}
}
