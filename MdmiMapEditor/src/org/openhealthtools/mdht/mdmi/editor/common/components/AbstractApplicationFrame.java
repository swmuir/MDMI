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
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.UserPreferences;
import org.openhealthtools.mdht.mdmi.editor.common.actions.ActionRegistry;
import org.openhealthtools.mdht.mdmi.editor.common.menus.XMLApplicationFactory;

/**
 * @author Conway
 * 
 * This is the main frame for most applications
 */
public abstract class AbstractApplicationFrame extends JFrame {
	private static final long serialVersionUID = -1;

	/** Resource for localization */
	private static ResourceBundle s_resBundle = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.components.Local");

	// listeners
	private WindowSizeListener    m_windowSizeListener    = new WindowSizeListener();
	private WindowStateListener   m_windowStateListener   = new WindowStateListener();

	// Other data
	private UserPreferences 	 m_preferences;


	public AbstractApplicationFrame() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		try {
			String applicationName = getApplicationName();
			SystemContext.setApplicationName(applicationName);
			SystemContext.setApplicationFrame(this);
			
			updateTitle(null);

			// set image
			URL url = getClass().getResource(s_resBundle.getString("AbstractApplicationFrame.frameIcon"));
			if (url != null) {
				setIconImage( new ImageIcon(url).getImage() );
			}

			// register actions
			String menuFileName = getMenuXMLFile();
			if (menuFileName != null) {
				XMLApplicationFactory  xmlFactory = new XMLApplicationFactory(menuFileName);
				xmlFactory.registerActions();
			}

			// build components
			buildUI();

		} catch (Exception e) {
			this.setVisible(true);
			ExceptionDetailsDialog.showException(this, e);
			System.exit(1);
		}
	}
	
	/** return the file containing the XML data used for configuring the menus */
	public abstract String getMenuXMLFile();
	
	/** set the title to the application name and file */
	public void updateTitle(String fileName) {
		String appName = getApplicationName();
		if (fileName == null) {
			setTitle(appName);
		} else {
			setTitle(MessageFormat.format(s_resBundle.getString("AbstractApplicationFrame.title"),
					appName, fileName));
		}
	}


	/** Get this application's name */
	public abstract String getApplicationName();


	/** Configure service information such as versions, factories, etc */
	public  void configureServiceInformation() {
		// default is to do nothing
	}

	/**
	 * Create UI Components
	 */
	protected void buildUI() {
		getContentPane().setLayout(new BorderLayout());
		
		// add listener for closing
		addWindowListener(new WindowListener());

		CursorManager cm = CursorManager.getInstance(this);

		try {
			cm.setWaitCursor();
			
			// set user preferences
			setUserPreferences( UserPreferences.getInstance(getApplicationName(), null) );
			
			// create menus
			createMenus();

			// create the main display
			createMainDisplay();

			// Set the size based on user preferences
			setWindowSize();

			// add size listener
			addComponentListener(m_windowSizeListener);
			addWindowStateListener(m_windowStateListener);


		} finally {
			cm.restoreCursor();
		}
	}
	
	/**
	 * Set the user preferences
	 */
	protected void setUserPreferences(UserPreferences pref) {
		m_preferences = pref;
	}
	
	/**
	 * Get the user preferences
	 */
	public UserPreferences getUserPreferences() {
		return m_preferences;
	}
	
	/**
	 * Create the menus (including toolbar items) from the xml file. The user
	 * needs to be logged in before this is called.
	 */
	private void createMenus() {
		try {
			String menuFileName = getMenuXMLFile();
			if (menuFileName == null) {
				return;
			}
			
			ActionRegistry.clearAllActions();  // remove any old menus

			// register actions
			XMLApplicationFactory  xmlFactory = new XMLApplicationFactory(menuFileName);
			xmlFactory.build();

			// add menu bar
			setJMenuBar(xmlFactory.getMenuBar());

			// add tool bar
			JPanel toolBar = createToolBar(xmlFactory);
			if (toolBar != null) {
				getContentPane().add(toolBar, BorderLayout.NORTH);
			}


		} catch (Exception e) {
			ExceptionDetailsDialog.showException(this, e);
		}
	}

	
	/** Set the window size based on user's preferences */
	protected void setWindowSize() {
		Dimension appSize = new Dimension(m_preferences.getApplicationWidth(),
				m_preferences.getApplicationHeight());
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// make sure application is within screen bounds
		appSize.width = Math.min(appSize.width, screenSize.width);
		appSize.height = Math.min(appSize.height, screenSize.height);
		setPreferredSize(appSize);
		setSize(appSize);
	}


	/** Create the toolbar */
	protected JPanel createToolBar(XMLApplicationFactory xmlFactory) {
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
	protected void createMainDisplay() {
		// default does nothing
	}


	/** clean up resources */
	@Override
	public void dispose() {
		super.dispose();

		// remove listeners
		removeComponentListener(m_windowSizeListener);
		removeWindowStateListener(m_windowStateListener);

		
		// remove all children so garbage collection works better
		try {
			WindowUtil.removeAllComponents(this);
		} catch (Exception ex) {
		}

		// close any remaining windows
		Frame[] frames = Frame.getFrames();
		for (Frame w : frames) {
			if (w == this) continue; // avoid recursion
			if (w.isVisible()) {
				try {
					w.dispose();
				} catch (Exception ex) {
				}
			}
		}

	}
	
	/** Start the application by presenting a login dialog, etc */
	public boolean startApplication(String[] args) {


		// configure service factories, etc
		configureServiceInformation();

		// show all components
		displayEditor();

		return true;

	}

	/** Display the editor on the screen, using the last known size */
	protected void displayEditor() {
		if (isVisible()) {
			return;
		}
		
		pack();
		if (m_preferences.isApplicationMaximized()) {
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			BaseDialog.centerOnScreen(AbstractApplicationFrame.this);
		}

		setVisible(true);
		toFront();
	}
	
	
	/** Called when window is closing. A return value of false will cancel the close operation */
	protected boolean okayToClose() {
		return true;
	}
	

	/** Window close listener - handle log out */
	private class WindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			try {
				// check and save changes
				boolean okayToExit = okayToClose();
				if (!okayToExit) {
					return;
				}

			} catch (Exception ex) {
				BaseDialog.showError(AbstractApplicationFrame.this, getTitle(),
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
	


}
