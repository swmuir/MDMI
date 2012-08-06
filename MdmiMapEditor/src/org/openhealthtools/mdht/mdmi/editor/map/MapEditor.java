/*******************************************************************************
* Copyright (c) 2012 Firestar Software, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Firestar Software, Inc. - initial API and implementation
*
* Author:
*     Sally Conway
*
*******************************************************************************/
/*
 * Created on Oct 31, 2005
 *
 */
package org.openhealthtools.mdht.mdmi.editor.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.actions.ActionRegistry;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.WindowUtil;
import org.openhealthtools.mdht.mdmi.editor.common.menus.XMLApplicationFactory;
import org.openhealthtools.mdht.mdmi.editor.map.actions.ExitAction;
import org.openhealthtools.mdht.mdmi.editor.map.editor.EditorPanel;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.editor.map.tree.MdmiModelTree;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;
import org.openhealthtools.mdht.mdmi.util.FileUtil;
import org.openhealthtools.mdht.mdmi.util.LogWriter;

/**
 * @author Conway
 * 
 * This is the main frame for the MDMI Map Editor. 
 */
public class MapEditor extends JFrame {
	private static final long serialVersionUID = -1;

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.Local");
	private static String	s_applicationName;

	private String m_savePath = "C:/Temp/";
	
	// Components
	private JSplitPane       m_mainPanel;
	private MdmiModelTree 	m_msgGroupTree;
	private JSplitPane 	    m_selectedPanel;
	private EditorPanel	 	    m_editor;
	private StatusPanel	    m_statusArea;
	

	// listeners
	private WindowSizeListener    m_windowSizeListener    = new WindowSizeListener();
	private WindowStateListener   m_windowStateListener   = new WindowStateListener();
	private DividerChangeListener m_dividerChangeListener = new DividerChangeListener();

	// Other data
	private UserPreferences 	 m_preferences;

   private Timer  m_timer = null;

	public MapEditor() {
		try {
			String applicationName = getApplicationName();
			SystemContext.setApplicationName(applicationName);
			SystemContext.setApplicationFrame(this);
			
			updateTitle(null);

			// set image
			URL url = getClass().getResource(s_res.getString("MapEditor.frameIcon"));
			if (url != null) {
				setIconImage( new ImageIcon(url).getImage() );
			}

			// register actions
			XMLApplicationFactory  xmlFactory = new XMLApplicationFactory(s_res.getString("MapEditor.menuXML"));
			xmlFactory.registerActions();

			// build components
			buildUI();

		} catch (Exception e) {
			this.setVisible(true);
			ExceptionDetailsDialog.showException(this, e);
			System.exit(1);
		}
	}
	
	/** set the title to the application name and file */
	public void updateTitle(String fileName) {
		String appName = getApplicationName();
		if (fileName == null) {
			setTitle(appName);
		} else {
			setTitle(MessageFormat.format(s_res.getString("MapEditor.title"),
					appName, fileName));
		}
	}


	/** Get this application's name */
	public static String getApplicationName() {
		if (s_applicationName == null) { 
			s_applicationName = s_res.getString("MapEditor.applicationName");
		}
		return s_applicationName;
	}


	/** Configure service information such as versions, factories, etc */
	public static void configureServiceInformation() {
		// TODO
	}

	/**
	 * Build the layout
	 * 
	 * @throws ETIServiceException
	 */
	private void buildUI() {
		getContentPane().setLayout(new BorderLayout());
		
		// add listener for closing
		addWindowListener(new WindowListener());

		createComponents();
	}

	/**
	 * Create the menus (including toolbar items) from the xml file. The user
	 * needs to be logged in before this is called.
	 */
	private void createMenus() {
		try {
			ActionRegistry.clearAllActions();  // remove any old menus
			XMLApplicationFactory xmlFactory = new XMLApplicationFactory(
					s_res.getString("MapEditor.menuXML"));
			xmlFactory.build();

			// add menu bar
			setJMenuBar(xmlFactory.getMenuBar());

			// add tool bar
			getContentPane().add(createToolBar(xmlFactory), BorderLayout.NORTH);


		} catch (Exception e) {
			ExceptionDetailsDialog.showException(this, e);
		}
	}

	/**
	 * Create UI Components
	 */
	private void createComponents() {
		CursorManager cm = CursorManager.getInstance(this);

		try {
			cm.setWaitCursor();
			m_preferences = UserPreferences.getInstance(getApplicationName(), null);
			// create menus
			createMenus();

			// create the main display
			createMainDisplay();

			// Set the size based on user preferences
			setWindowSize();

			// add size listener
			addComponentListener(m_windowSizeListener);
			addWindowStateListener(m_windowStateListener);
			
			// add listener for splitters
			m_mainPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, m_dividerChangeListener);
			m_selectedPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, m_dividerChangeListener);
			
			// start manager
			SelectionManager.getInstance().startManagement(m_msgGroupTree, m_editor, m_statusArea);
			
			// start timer
            try {
                saveDataPeriodically();
            } catch (IOException e) {
                //todo: ExceptionHandler.handleSilently()
                e.printStackTrace();
            }
			
//			// Prompt for a file to open
//			ModelIOUtilities.loadModelFromFile();
		} finally {
			cm.restoreCursor();
		}
	}
	
	/** Start a timer that runs every 5 minutes to save the data in progress */
	private void saveDataPeriodically() throws IOException {
		m_timer = new Timer(true);

		// File name "Edits_05-06-10.15.45.xmi"
		String saveFileName = MessageFormat.format(s_res.getString("MapEditor.tempFileName"),
				new Date());

        FileUtil.createDir(m_savePath);
		final File saveFile = new File(m_savePath + saveFileName );

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				List<MessageGroup> groups = 
					SelectionManager.getInstance().getEntitySelector().getMessageGroups();
				try {
					XMIWriterDirect.write(saveFile, groups);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		// save every 5 minutes
		int refreshTime = 5*60*1000;
		m_timer.schedule(task, refreshTime, refreshTime);
	}
	
	/** Set the window size based on user's preferences */
	private void setWindowSize() {
		Dimension appSize = new Dimension(m_preferences.getApplicationWidth(),
				m_preferences.getApplicationHeight());
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// make sure application is within screen bounds
		appSize.width = Math.min(appSize.width, screenSize.width);
		appSize.height = Math.min(appSize.height, screenSize.height);
		setPreferredSize(appSize);
		setSize(appSize);
		
		// Adjust splitters appropriately
		m_mainPanel.setDividerLocation(m_preferences.getMainDividerLocation());
		m_selectedPanel.setDividerLocation(m_preferences.getSelectedItemDividerLocation());
	}


	/** Create the toolbar */
	private JPanel createToolBar(XMLApplicationFactory xmlFactory) {
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JToolBar[] toolbars = xmlFactory.getToolBars();

		for (int i = 0; i < toolbars.length; i++) {
			toolBarPanel.add(toolbars[i]);
		}

		return toolBarPanel;
	}

	/**
	 * Create the main display
	 * 
	 * @throws ETIServiceException
	 */
	private void createMainDisplay() {

		// Configuration Editor
		//  _____________________________
		// | _______  __________________ |
		// ||       || Item             ||
		// ||item   || Editor           ||
		// ||select ||__________________||
		// ||       | __________________ |
		// ||       || Status Region    ||
		// ||_______||__________________||
		// |_____________________________|


		// Left Side
		m_msgGroupTree = new MdmiModelTree();
		
		// Right Side
		m_editor = new EditorPanel();	// Top
		m_statusArea = new StatusPanel();	// Bottom;
		m_selectedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_editor, m_statusArea);
		m_selectedPanel.setResizeWeight(1.0);	// give weight to top component
		m_selectedPanel.setOneTouchExpandable(true);
		

		m_mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_msgGroupTree,
				m_selectedPanel);
		m_mainPanel.setOneTouchExpandable(true);

		// add main panel to center of frame
		getContentPane().add(m_mainPanel, BorderLayout.CENTER);
	}


	/** clean up resources */
	@Override
	public void dispose() {
//		System.out.println("Calling super.dispose():");
		super.dispose();

		// remove listeners
//		System.out.println(" Removing listeners");
		removeComponentListener(m_windowSizeListener);
		removeWindowStateListener(m_windowStateListener);
		m_mainPanel.removePropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, m_dividerChangeListener);
		m_selectedPanel.removePropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, m_dividerChangeListener);

//		System.out.println(" End Management");
		SelectionManager.getInstance().endManagement();
		
		// remove all children so garbage collection works better
//		System.out.println(" Removing components");
		try {
			WindowUtil.removeAllComponents(this);
		} catch (Exception ex) {
		}

		// close any remaining windows
//		System.out.println(" Closing frames");
		Frame[] frames = Frame.getFrames();
		for (Frame w : frames) {
			if (w instanceof MapEditor) continue; // avoid recursion
			if (w.isVisible()) {
				try {
					w.dispose();
				} catch (Exception ex) {
				}
			}
		}

//		System.out.println(" Garbage collecting");
//		System.gc();
	}
	
	/** Start the application by presenting a login dialog, etc */
	public boolean startApplication(String[] args) {
		//      AppHelp.getInstance().setHelpFileName(HelpIDStrings.HELP_NAME);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// configure service factories, etc
		configureServiceInformation();

		// show all components
		displayEditor();

		// Prompt for a file to open
		ModelIOUtilities.loadModelFromFile();
		return true;

	}

	/** Display the editor on the screen, using the last known size */
	private void displayEditor() {
		if (isVisible()) {
			return;
		}
		
		pack();
		if (m_preferences.isApplicationMaximized()) {
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			BaseDialog.centerOnScreen(MapEditor.this);
		}

		setVisible(true);
		toFront();
	}
	
	
	/** Write to the console window */
	public void writeToConsole(String text) {
		m_statusArea.writeConsole(text);
	}
	

	/** Window close listener - handle log out */
	private class WindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			try {
				// check and save changes
				boolean okayToExit = ExitAction.checkForChanges();
				if (!okayToExit) {
					return;
				}

			} catch (Exception ex) {
				BaseDialog.showError(MapEditor.this, getTitle(),
						ex.getLocalizedMessage());
			}
			dispose();
			System.exit(0);
		}
	}

	/** listener for size changes - save new size */
	private class WindowSizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			// save size
			if (m_preferences != null) {
				m_preferences.putApplicationWidth(getWidth());
				m_preferences.putApplicationHeight(getHeight());
			}
		}
	}
		
	/** listener for minimize/maximize - save state */
	private class WindowStateListener extends WindowAdapter {
		@Override
		public void windowStateChanged(WindowEvent e) {
			// save state
			m_preferences.putApplicationMaximized((e.getNewState()&JFrame.MAXIMIZED_BOTH) != 0 );
		}
	}
	
	/** listener for split-pane divider location change */
	private class DividerChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(event.getPropertyName())) {
				Integer value = (Integer)event.getNewValue();
				if (event.getSource() == m_mainPanel) {
					m_preferences.putMainDividerLocation(value.intValue());
				} else if (event.getSource() == m_selectedPanel) {
					m_preferences.putSelectedItemDividerLocation(value.intValue());
				}
			}
		}
	}

    /**
     * Starting point for application.
     * Optional argument: -qa
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

        MapEditor editor = new MapEditor();
        boolean started = editor.startApplication(args);
        if (!started) {
            System.exit(0);
        }
    }


}
