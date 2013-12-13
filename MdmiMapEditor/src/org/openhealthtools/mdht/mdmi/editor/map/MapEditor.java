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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.components.AbstractApplicationFrame;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
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
public class MapEditor extends AbstractApplicationFrame {

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.Local");
	private static String	s_applicationName;


	// Components
	private JSplitPane       m_mainPanel;
	private MdmiModelTree 	m_msgGroupTree;
	private JSplitPane 	    m_selectedPanel;
	private EditorPanel	 	    m_editor;
	private StatusPanel	    m_statusArea;


	// listeners
	private DividerChangeListener m_dividerChangeListener;

	// Other data
	private org.openhealthtools.mdht.mdmi.editor.map.UserPreferences m_customPreferences;

	private Timer  m_timer;

	public MapEditor() {

	}


	@Override
	public String getMenuXMLFile() {
		return s_res.getString("MapEditor.menuXML");
	}



	@Override
	public String getApplicationName() {
		return getMapEditorName();
	}

	public static String getMapEditorName() {
		if (s_applicationName == null) { 
			s_applicationName = s_res.getString("MapEditor.applicationName");
		}
		return s_applicationName;
	}



	/**
	 * Create UI Components
	 */
	@Override
	protected void buildUI() {
		super.buildUI();

		CursorManager cm = CursorManager.getInstance(this);

		try {
			cm.setWaitCursor();

			// add listener for splitters
			m_dividerChangeListener = new DividerChangeListener();
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

		} finally {
			cm.restoreCursor();
		}
	}

	
	@Override
	protected void setUserPreferences(UserPreferences pref) {
		// use our own preferences
		m_customPreferences = new org.openhealthtools.mdht.mdmi.editor.map.UserPreferences(getApplicationName(), null);
		super.setUserPreferences(m_customPreferences);
	}


	/** Start a timer that runs every 5 minutes to save the data in progress */
	private void saveDataPeriodically() throws IOException {
		m_timer = new Timer(true);

		// File name "Edits_05-06-10.15.45.xmi"
		String saveFileName = MessageFormat.format(s_res.getString("MapEditor.tempFileName"),
				new Date());

		String savePath = "C:/Temp/";
		FileUtil.createDir(savePath);
		final File saveFile = new File(savePath + saveFileName );

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				List<MessageGroup> groups = 
						SelectionManager.getInstance().getEntitySelector().getMessageGroups();
				try {
					XMIWriterDirect.write(saveFile, groups);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		// save every 5 minutes
		int refreshTime = 5*60*1000;
		m_timer.schedule(task, refreshTime, refreshTime);
	}

	/** Set the window size based on user's preferences */
	@Override
	protected void setWindowSize() {
		super.setWindowSize();

		// Adjust splitters appropriately
		m_mainPanel.setDividerLocation(m_customPreferences.getMainDividerLocation());
		m_selectedPanel.setDividerLocation(m_customPreferences.getSelectedItemDividerLocation());
	}



	/**
	 * Create the main display
	 * 
	 */
	@Override
	protected void createMainDisplay() {

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
		// remove listeners
		m_mainPanel.removePropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, m_dividerChangeListener);
		m_selectedPanel.removePropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, m_dividerChangeListener);

		SelectionManager.getInstance().endManagement();

		super.dispose();

	}

	/** Start the application by presenting a login dialog, etc */
	@Override
	public boolean startApplication(String[] args) {

		super.startApplication(args);

		// Prompt for a file to open
		ModelIOUtilities.loadModelFromFile();
		return true;

	}



	/** Write to the console window */
	public void writeToConsole(String text) {
		m_statusArea.writeConsole(text);
	}

	/** Called when window is closing. A return value of false will cancel the close operation */
	@Override
	protected boolean okayToClose() {
		// check and save changes
		boolean okayToExit = ExitAction.checkForChanges();
		return okayToExit;
	}


	/** listener for split-pane divider location change */
	private class DividerChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(event.getPropertyName())) {
				Integer value = (Integer)event.getNewValue();
				if (event.getSource() == m_mainPanel) {
					m_customPreferences.putMainDividerLocation(value.intValue());
				} else if (event.getSource() == m_selectedPanel) {
					m_customPreferences.putSelectedItemDividerLocation(value.intValue());
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
