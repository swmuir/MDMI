package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openhealthtools.mdht.mdmi.editor.common.Standards;


/** A component to pick a rule language.
 * A property change event will be fired when the selection changes. */
public class RuleLanguageSelector extends JPanel implements IEditorField, ActionListener {	
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.editor.Local");

	// language property (for property change listeners)
	public static final String RULE_LANGUAGE = "RuleLanguage";
	
	// supported languages
	public static final String JAVA_SCRIPT = s_res.getString("RuleLanguageSelector.javaScript");
	public static final String NRL = s_res.getString("RuleLanguageSelector.NRL");
	
	
	private GenericEditor m_parentEditor = null;
	
	// since we only support two rules (so far), we'll use radio buttons. JavaScript is the default
	ButtonGroup m_buttonGroup = new ButtonGroup();
	JRadioButton m_javaScriptBtn = new JRadioButton(JAVA_SCRIPT, true);
	JRadioButton m_nrlBtn = new JRadioButton(NRL, false);

	/* Create a selector with no parent editor. */
	public RuleLanguageSelector() {
		this(null);
	}

	/* Create a selector with a parent editor */
	public RuleLanguageSelector(GenericEditor parentEditor) {
		m_parentEditor = parentEditor;
		buildUI();
	}
	
	/** Return the selected language */
	public String getLanguage() {
		if (m_buttonGroup.getSelection() == m_nrlBtn.getModel()) {
			return NRL;
		}
		return JAVA_SCRIPT;
	}
	
	/** Set the selected language */
	public void setLanguage(String language) {
		if (NRL.equals(language)) {
			m_nrlBtn.setSelected(true);
		}
		else {
			m_javaScriptBtn.setSelected(true);
		}
	}
	
	/** Lay out the components:
	 * 
	 *    (o) Java Script   ( ) NRL
	 */
	private void buildUI() {
		setLayout(new FlowLayout(FlowLayout.LEFT, Standards.LEFT_INSET, 0));
		
		m_buttonGroup.add(m_javaScriptBtn);
		m_buttonGroup.add(m_nrlBtn);
		
		add(m_javaScriptBtn);
		add(m_nrlBtn);
	}
	
	/** return an array of the supported rule languages */
	public static String[] getSupportedLanguages() {
		return new String[] {JAVA_SCRIPT, NRL};
	}
	

	@Override
	public void addNotify() {
		super.addNotify();
		m_javaScriptBtn.addActionListener(this);
		m_nrlBtn.addActionListener(this);
	}

	@Override
	public void removeNotify() {
		m_javaScriptBtn.removeActionListener(this);
		m_nrlBtn.removeActionListener(this);
		super.removeNotify();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (m_parentEditor != null) {
			m_parentEditor.setModified(true);
		}
		if (e.getSource() == m_nrlBtn) {
			firePropertyChange(RULE_LANGUAGE, JAVA_SCRIPT, NRL);
		} else {
			firePropertyChange(RULE_LANGUAGE, NRL, JAVA_SCRIPT);
		}
	}

	/////////////////////////////////////////////////////
	// IEditorField Methods
	/////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		return getLanguage();
	}

	@Override
	public void setDisplayValue(Object value) throws DataFormatException {
		if (value == null) {
			// ignore
		} else if (value instanceof Boolean) {
			setLanguage(value.toString());
		} else {
			// '{0}' is not a {1}.
			throw new DataFormatException(MessageFormat.format(s_res.getString("GenericEditor.dataFormatExceptionFormat"),
					value, "String"));
		}
	}

	@Override
	public void setReadOnly() {
		m_javaScriptBtn.setEnabled(false);
		m_nrlBtn.setEnabled(false);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void highlightText(String text, Color highlightColor) {
		// N/A
	}

}
