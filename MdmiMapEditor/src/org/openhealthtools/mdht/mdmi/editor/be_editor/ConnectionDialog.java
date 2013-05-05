package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;

/** Provide a dialog for connecting to the server */
public class ConnectionDialog extends BaseDialog {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");
	
	// UserPreference keys
	public static final String SERVER_URL = "ServerURL";
	public static final String SERVER_TOKEN = "ServerToken";
	
	private String m_applicationName;
	private UserPreferences m_pref;
	private String [] m_lastUrls;
	
	private JComboBox m_urlSelector   = new JComboBox();
	private JTextField m_token = new JTextField(30);

	private ActionListener m_actionListener = new FieldActionListener();
	private DocumentListener m_textListener = new TextFieldListener();
	
	public ConnectionDialog(Frame owner, String appName) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		m_applicationName = appName;
		
		String title = MessageFormat.format(s_res.getString("ConnectionDialog.title"), appName);
		setTitle(title);

		m_pref = UserPreferences.getInstance(appName, null);

		buildUI();
		
		pack();
	}
	
	private void buildUI() {
		//  ---------
		// | image   | Please connect to XXX
		// |         | URL:    [_______________v]
		// |         | Token:  [________________]
		//  ---------          
		
		// Populate URL selector with new-line separated values
		m_urlSelector.setEditable(true);
		String urlValuesString = m_pref.getValue(SERVER_URL, "http://localhost:8080/MdmiSvc");
		m_lastUrls = urlValuesString.split("\\n");
		for (String url : m_lastUrls) {
			m_urlSelector.addItem(url);
		}
		
		// Populate token
		String tokenValue = m_pref.getValue(SERVER_TOKEN, "KenLord-MDMI2013");
		m_token.setText(tokenValue);
		
		// Show image on left
		URL imageURL = getClass().getResource(s_res.getString("ConnectionDialog.icon"));
		if (imageURL != null) {
			ImageIcon icon = new ImageIcon(imageURL);
			JLabel image = new JLabel(icon);
			getContentPane().add(image, BorderLayout.WEST);
		}
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.WEST;

		// Please Log In to {0}
		String text = MessageFormat.format(s_res.getString("ConnectionDialog.text"), m_applicationName);
		JLabel textMessage = new JLabel(text);
		gbc.gridwidth = 2;
		mainPanel.add(textMessage, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// User Name
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("ConnectionDialog.urlLabel")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_urlSelector, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		
		// Token
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("ConnectionDialog.tokenLabel")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_token, gbc);
		
		// pad it
		mainPanel.setBorder(Standards.createEmptyBorder());

		// add listeners
		m_urlSelector.addActionListener(m_actionListener);
		((JTextField)m_urlSelector.getEditor().getEditorComponent()).getDocument().addDocumentListener(m_textListener);
		m_token.getDocument().addDocumentListener(m_textListener);
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		setDirty(true);
	}

	@Override
	public boolean isDataValid() {
		return (!getURLString().isEmpty() && !getToken().isEmpty());
	}
	
	public URI getURL() throws IllegalArgumentException {
		String stringValue = getURLString();
		return URI.create(stringValue);
	}
	
	public String getURLString() {
		JTextField ed =  (JTextField)m_urlSelector.getEditor().getEditorComponent();
		return ed.getText().trim();
	}
	
	public String getToken() {
		return m_token.getText().trim();
	}

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_urlSelector.removeActionListener(m_actionListener);
		((JTextField)m_urlSelector.getEditor().getEditorComponent()).getDocument().removeDocumentListener(m_textListener);
		m_token.getDocument().removeDocumentListener(m_textListener);
	}
	
	@Override
	protected void okButtonAction() {
		// Validate URL
		try {
			getURL();
		} catch (IllegalArgumentException ex) {
			String message = "'" + getURLString() + "' is not a valid URL";
			//m_urlSelector.selectAll();
			m_urlSelector.requestFocus();
			
			JOptionPane.showMessageDialog(this, message, "Invalid Data", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// Save values
		String selectedURL = getURLString();
		StringBuffer buf = new StringBuffer();
		buf.append(selectedURL);
		// append the other URLs (up to 8)
		int count = 1;
		for (int i=0; i<m_lastUrls.length && count < 8; i++) {
			if (!m_lastUrls[i].equals(selectedURL)) {
				buf.append("\n").append(m_lastUrls[i]);
				count++;
			}
		}
		m_pref.putValue(SERVER_URL, buf.toString());
		m_pref.putValue(SERVER_TOKEN, getToken());
		
		
		super.okButtonAction();
	}
	
	private class FieldActionListener implements ActionListener {

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
