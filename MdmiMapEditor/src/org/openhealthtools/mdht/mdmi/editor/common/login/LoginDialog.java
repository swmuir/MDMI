package org.openhealthtools.mdht.mdmi.editor.common.login;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;

/** Provide a dialog for logging in */
public class LoginDialog extends BaseDialog {
	
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.login.Local");
	
	private String m_applicationName;
	
	private JTextField m_userName = new JTextField(30);
	private JPasswordField m_password = new JPasswordField(30);
	private JCheckBox m_rememberBox = new JCheckBox(s_res.getString("LoginDialog.rememberLabel"));

	private DocumentListener m_textListener = new TextFieldListener();
	
	public LoginDialog(Frame owner, String appName) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		m_applicationName = appName;
		
		String title = MessageFormat.format(s_res.getString("LoginDialog.title"), appName);
		setTitle(title);


		buildUI();
		
		pack();
	}
	
	private void buildUI() {
		//  ---------
		// | image   | Please log in to XXX
		// |         | User Name: [________________]
		// |         | Password:  [________________]
		//  ---------          [x] Remember user name
		
		// Show image on left
		URL url = getClass().getResource(s_res.getString("LoginDialog.icon"));
		if (url != null) {
			ImageIcon icon = new ImageIcon(url);
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
		String text = MessageFormat.format(s_res.getString("LoginDialog.text"), m_applicationName);
		JLabel textMessage = new JLabel(text);
		gbc.gridwidth = 2;
		mainPanel.add(textMessage, gbc);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0;
		gbc.gridy++;
		
		// User Name
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("LoginDialog.userNameLabel")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_userName, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		
		// Password
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel(s_res.getString("LoginDialog.passwordLabel")), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(m_password, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		
		// Remember me
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.EAST;
		mainPanel.add(m_rememberBox, gbc);
		
		// pad it
		mainPanel.setBorder(Standards.createEmptyBorder());

		// add listeners
		m_userName.getDocument().addDocumentListener(m_textListener);
		m_password.getDocument().addDocumentListener(m_textListener);
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	@Override
	public boolean isDataValid() {
		return (!getUserName().isEmpty() && !getPassword().isEmpty());
	}
	
	public String getUserName() {
		return m_userName.getText().trim();
	}
	
	public String getPassword() {
		char [] passChars = m_password.getPassword();
		String password = new String(passChars);
		return password;
	}

	@Override
	public void dispose() {
		super.dispose();
		
		// remove all listeners
		m_userName.getDocument().removeDocumentListener(m_textListener);
		m_password.getDocument().removeDocumentListener(m_textListener);
	}
	
	@Override
	protected void okButtonAction() {
		super.okButtonAction();
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
