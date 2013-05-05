package org.openhealthtools.mdht.mdmi.editor.be_editor;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.openhealthtools.mdht.mdmi.editor.be_editor.tables.TableEntry;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.AbstractApplicationFrame;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.map.MapEditor;
import org.openhealthtools.mdht.mdmi.model.DTSPrimitive;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.util.LogWriter;


/** A utility for editing Business Elements and Data Types on a server */
public class BEEditor extends AbstractApplicationFrame  {

	// make it a singleton
    private static BEEditor s_instance = null;

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.be_editor.Local");
	private static String s_applicationName = null;
	
	private JTabbedPane       m_mainPanel;
	private BEDisplayPanel 	  m_berPanel;
	private DataTypeDisplayPanel   m_datatypePanel;


	private BEEditor() {
	}
	
	public static BEEditor getInstance() {
		if (s_instance == null) {
			s_instance = new BEEditor();
		}
		return s_instance;
	}

	
	@Override
	public String getMenuXMLFile() {
		return s_res.getString("BEEditor.menuXML");
	}


	@Override
	public String getApplicationName() {
		if (s_applicationName == null) { 
			s_applicationName = s_res.getString("BEEditor.applicationName");
		}
		return s_applicationName;
	}


	@Override
	public boolean startApplication(String[] args) {
		// Connect to Server
		ConnectionDialog dlg = new ConnectionDialog(this, getApplicationName());
		int rc = dlg.display(this);
		if (rc != BaseDialog.OK_BUTTON_OPTION) {
			return false;
		}
		
		ServerInterface.getInstance().connect(dlg.getURL(), dlg.getToken());
		return super.startApplication(args);
	}


	@Override
	public void configureServiceInformation() {
		// TODO Auto-generated method stub
		super.configureServiceInformation();
	}


	@Override
	protected void createMainDisplay() {
		// The Editor
		m_mainPanel = new JTabbedPane();
		
		// Two panels
		m_berPanel = new BEDisplayPanel();
		m_datatypePanel = new DataTypeDisplayPanel();
		m_mainPanel.addTab("Data Types", m_datatypePanel);
		m_mainPanel.addTab("Business Elements", m_berPanel);

		// add main panel to center of frame
		getContentPane().add(m_mainPanel, BorderLayout.CENTER);
	}

	public BEDisplayPanel getBEDisplayPanel() {
		return m_berPanel;
	}
	
	public DataTypeDisplayPanel getDataTypeDisplayPanel() {
		return m_datatypePanel;
	}
	
	
	// find the datatype object with this name
	public MdmiDatatype findMdmiDatatype(String name)
	{
		// is it a primitive type
		for (DTSPrimitive primitive : DTSPrimitive.ALL_PRIMITIVES) {
			if (primitive.getDescription().equalsIgnoreCase(name)) {
				return primitive;
			}
		}
		
		// search locally
		List <TableEntry> allDatatypes = m_datatypePanel.getTableModel().getAllEntries();
		for (TableEntry entry : allDatatypes) {
			Object userObject = entry.getUserObject();
			if (userObject instanceof MdmiDatatype &&
					name.equalsIgnoreCase(((MdmiDatatype)userObject).getName())) {
				return (MdmiDatatype)userObject;
			}
		}

		// finally, look it up
		MdmiDatatype datatype = null;
		try {
			datatype = ServerInterface.getInstance().getDatatype(name);
		} catch (Exception ex) {
			// don't care
		}
		
		return datatype;
	}


	/** clean up resources */
	@Override
	public void dispose() {
		super.dispose();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        // initialize LogWriter
        LogWriter lw = new LogWriter(Level.INFO, new File("./logs"), true, false);
        SystemContext.setLogWriter(lw);

        try {
            String lAndF = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lAndF);
        } catch (Exception e) {
            SystemContext.getLogWriter().loge(e, "Error setting look and feel for " + MapEditor.class.getName());
        }

        BEEditor editor = BEEditor.getInstance();
        boolean started = editor.startApplication(args);
        if (!started) {
            System.exit(0);
        }
	}

}
