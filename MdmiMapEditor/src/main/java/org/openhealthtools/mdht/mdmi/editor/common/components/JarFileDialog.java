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
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openhealthtools.mdht.mdmi.editor.common.JarClassLoaderMgr;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.util.JarClassLoader;

/** A dialog for selecting a jar file and class */
public class JarFileDialog extends BaseDialog {
	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.components.Local");

	private static String s_lastFileLocation = null;
	
	private JTextField m_jarFileName = new JTextField(40);
	
	private DefaultListModel m_listModel = new DefaultListModel();
	private JList	   m_classNameList = new JList(m_listModel);
	

	private JLabel     m_classesLabel = new JLabel(s_res.getString("JarFileDialog.classesOne"));
	private JButton    m_browseButton = new JButton(s_res.getString("JarFileDialog.browseBtn"));
	
	private BrowseBtnListener m_browseListener = new BrowseBtnListener();
	private SelectionListener m_selectionListener = new SelectionListener();
	
	private ClassFilter m_classFilter = null;
	private String m_startingFolder   = s_lastFileLocation;
	
	public static JarFileDialog getJarFileDialog(JComponent component) {
		Container top = component.getTopLevelAncestor();
		JarFileDialog dlg;
		if (top instanceof JDialog) {
			dlg = new JarFileDialog((JDialog)top);
		} else if (top instanceof JFrame) {
			dlg = new JarFileDialog((JFrame)top);
		} else if (SystemContext.getApplicationFrame() instanceof JFrame) {
			dlg = new JarFileDialog((JFrame)SystemContext.getApplicationFrame());
		} else {
			dlg = new JarFileDialog((JFrame)null);
		}
		return dlg;
	}

	public JarFileDialog(JFrame parent) {
		super(parent);
		buildUI();
		pack();
	}
	
	public JarFileDialog(JDialog parent) {
		super(parent);
		buildUI();
		pack();
	}
	
	/** Set the starting folder for the FileSelector dialog */
	public void setStartingFolder(String fileName) {
		m_startingFolder = (fileName == null) ? s_lastFileLocation : fileName;
	}
	
	/** Allow single or multiple selection of classes. By default, only single selection is allowed */
	public void setAllowsMultipleClassSelection(boolean multiples) {
		if (multiples) {
			m_classNameList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// Select One or More
			m_classesLabel.setText(s_res.getString("JarFileDialog.classesMultiples"));
		} else {
			// Select One
			m_classNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			m_classesLabel.setText(s_res.getString("JarFileDialog.classesOne"));
		}
	}
	
	/** set the class filter */
	public void setFileFilter(ClassFilter fileFilter) {
		m_classFilter = fileFilter;
	}


	private void buildUI() {
		setAllowsMultipleClassSelection(false);
		
		m_jarFileName.setEditable(false);	// require browse
		
		/*
		 *  Select Jar File: 
		 *  [______________________]  [Browse...]
		 *  
		 *  Available Classes:
		 *   ----------------------------------
		 *  |  Class 1                          |
		 *  |  Class 2                          |
		 *  |  Class 3                          |
		 *  |  Class 4                          |
		 *   -----------------------------------
		 * 
		 */

		JPanel main = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		
		JLabel selectLabel = new JLabel(s_res.getString("JarFileDialog.selectJar"));
		main.add(selectLabel, gbc);
		gbc.gridy++;
		
		gbc.insets.top = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(m_jarFileName, gbc);
		gbc.gridx++;
		gbc.insets.left = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(m_browseButton, gbc);
		

		gbc.insets = Standards.getInsets();
		gbc.gridx = 0;
		gbc.gridy++;
		main.add(m_classesLabel, gbc);
		gbc.gridy++;
		
		gbc.insets.top = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		JScrollPane scroller = new JScrollPane(m_classNameList);
		main.add(scroller, gbc);

		getContentPane().add(main, BorderLayout.CENTER);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		m_classNameList.addListSelectionListener(m_selectionListener);
		m_browseButton.addActionListener(m_browseListener);
	}
	
	@Override
	public void removeNotify() {
		m_classNameList.removeListSelectionListener(m_selectionListener);
		m_browseButton.removeActionListener(m_browseListener);
		super.removeNotify();
	}
	
	public String getJarFileName() {
		return m_jarFileName.getText().trim();
	}
	
	/** Set the jar file name. This will load the classes that match the filter criteria */
	public void setJarFileName(String jarName) throws IOException {
		m_jarFileName.setText(jarName);
		
		// get classes
		JarFile jarFile = new JarFile(jarName);
		JarClassLoader loader = JarClassLoaderMgr.getJarClassLoader(new File(jarName));
		
		m_listModel.clear();

        Enumeration<JarEntry> jes = jarFile.entries();
        while( jes.hasMoreElements() ) {
           JarEntry je = jes.nextElement();
           String entryName = je.getName();

           if( entryName.endsWith(".class") ) {
        	   // check
        	   String className = entryName.substring( 0, entryName.indexOf(".class") );
        	   className = className.replace( '/', '.' );
        	   if (m_classFilter == null || m_classFilter.accept(loader.findClass(className))) {
        			m_listModel.addElement(className);
        	   }
           }
        }
        m_classNameList.revalidate();
	}
	
	/** If single selection, this will be the one class name, or null. If multiple
	 * selection, this will be the first class on the list.
	 * @return
	 */
	public String getClassName() {
		Object sel = m_classNameList.getSelectedValue();
		if (sel != null) {
			return sel.toString();
		}
		return null;
	}
	
	/** Get all selected class names
	 */
	public String[] getClassNames() {
		Object [] selections = m_classNameList.getSelectedValues();
		String [] classNames = new String[selections.length];
		for (int i=0; i<selections.length; i++) {
			classNames[i] = selections[i].toString();
		}
		return classNames;
	}

	@Override
	public boolean isDataValid() {
		return (getJarFileName().length() > 0) && 
			(getClassName() != null);
	}
	
	private class SelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			setDirty(true);
		}
	}

	
	private class BrowseBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			CursorManager cm = CursorManager.getInstance(JarFileDialog.this);
			try {
				cm.setWaitCursor();
				
				JFileChooser chooser = new JFileChooser(m_startingFolder);
			
			    chooser.setDialogTitle(s_res.getString("JarFileDialog.fileSelectorTitle"));
			    FileFilter filter = new FileNameExtensionFilter(s_res.getString("JarFileDialog.fileSelectorFilter"),
			    		s_res.getString("JarFileDialog.fileSelectorFilterJar"), 
			    		s_res.getString("JarFileDialog.fileSelectorFilterZip"));
			    
			    chooser.addChoosableFileFilter(filter);
			    chooser.setAcceptAllFileFilterUsed(false);

				int returnVal = chooser.showOpenDialog(JarFileDialog.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					String fileName = file.getAbsolutePath();
					setJarFileName(fileName);
					s_lastFileLocation = fileName;
				}
				
				chooser.resetChoosableFileFilters();
				
			} catch (Exception ex) {
				ExceptionDetailsDialog.showException(JarFileDialog.this, ex);

			} finally {
				cm.restoreCursor();
			}
		}
	}
	
	/** Class filter */
	public static abstract class ClassFilter {
		public boolean accept(Class<?> theClass) {
			return true;
		}
	}


	public static void main(String [] args) {
		try {
			String lAndF = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lAndF);
		} catch (Exception e) {
		}
		JFrame dummyFrame = new JFrame();
		JarFileDialog dlg = new JarFileDialog(dummyFrame);
		dlg.display();
		System.exit(0);
	}
}
