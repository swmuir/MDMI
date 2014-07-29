package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.openhealthtools.mdht.mdmi.MdmiValueSet;
import org.openhealthtools.mdht.mdmi.MdmiValueSetMap;
import org.openhealthtools.mdht.mdmi.MdmiValueSetsHandler;
import org.openhealthtools.mdht.mdmi.editor.common.Standards;
import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.BaseDialog;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;

/** A Dialog for creating bi-directional mappings between value sets.
 * The value sets can be initialized from CSV files if necessary.
 * @author Sally Conway
 *
 */
public class ValueSetMapEditor extends BaseDialog {
	/** Resource for localization */
	protected static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.map.tools.Local");
	
	private MdmiValueSetsHandler m_handler;
	private MdmiValueSet m_srcValueSet;
	private MdmiValueSet m_targetValueSet;
	
	// From -> To / From <-> To
	private boolean m_biDirectional = true;
	
	private JComponent m_mainPanel;
	private JSplitPane m_tableSplitter = null;	// only used if bi-directional

	private ValueSetMapPanel m_srcToTargetTable = null;
	private ValueSetMapPanel m_targetToSrcTable = null;	// only used if bi-directional
	private JLabel m_mappingsLabel = new JLabel("Mappings");	// only used if bi-directional
	
	private ActionListener m_importBtnListener = new ImportButtonListener();
	private PropertyChangeListener m_tableListener = new TableDataListener();

	/**
	 * Create an Editor for mapping between two value sets.
	 * @param owner
	 * @param handler
	 * @param srcValueSetName
	 * @param targetValueSetName
	 */
	public ValueSetMapEditor(Frame owner, boolean biDirectional, MdmiValueSetsHandler handler, String srcValueSetName, String targetValueSetName) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		initDialog(handler, biDirectional, srcValueSetName, targetValueSetName);
	}

	/**
	 * Create an Editor for mapping between two value sets.
	 * @param owner
	 * @param handler
	 * @param srcValueSetName
	 * @param targetValueSetName
	 */
	public ValueSetMapEditor(Dialog owner, boolean biDirectional, MdmiValueSetsHandler handler, String srcValueSetName, String targetValueSetName) {
		super(owner, BaseDialog.OK_CANCEL_OPTION);
		initDialog(handler, biDirectional, srcValueSetName, targetValueSetName);
	}

	/** initialize dialog */
	private void initDialog(MdmiValueSetsHandler handler, boolean biDirectional, String srcValueSetName, String targetValueSetName) {
		setTitle("Value Set Editor");
		m_handler = handler;
		m_biDirectional = biDirectional;
		
		buildUI();
		setValueSetMap( srcValueSetName,  targetValueSetName);
		pack(new Dimension(600, 700));
	}
	
	/** use the maps to build the tables */
	private void setValueSetMap(String srcValueSetName, String targetValueSetName) {

		// Make sure value sets exist
		MdmiValueSet srcValueSet = m_handler.getValueSet(srcValueSetName);
		if (srcValueSet == null) {
			// create if it doesn't exist
			srcValueSet = new MdmiValueSet(m_handler, srcValueSetName);
		}
		MdmiValueSet targetValueSet = m_handler.getValueSet(targetValueSetName);
		if (targetValueSet == null) {
			// create if it doesn't exist
			targetValueSet = new MdmiValueSet(m_handler, targetValueSetName);
		}
		
		// Make sure value set maps exist
		MdmiValueSetMap srcToTargetMap = m_handler.getValueSetMap(srcValueSet.getName() + "." + targetValueSet.getName());
		if (srcToTargetMap == null) {
			srcToTargetMap = new MdmiValueSetMap(m_handler, srcValueSet, targetValueSet);
			m_handler.addValueSetMap(srcToTargetMap);
		}
		MdmiValueSetMap targetToSrcMap = m_handler.getValueSetMap(targetValueSet.getName() + "." + srcValueSet.getName());
		if (m_biDirectional) {
			if (targetToSrcMap == null) {
				targetToSrcMap = new MdmiValueSetMap(m_handler, targetValueSet, srcValueSet);
				m_handler.addValueSetMap(targetToSrcMap);
			}
			
			// set label text (in top section)
			m_mappingsLabel.setText("<html>Mappings between <b>" + srcValueSet.getName()
					+ "</b> and <b>" + targetValueSet.getName() + "</b></html>");
		}
		
		/////////////////////////////////////
		// set up the tables
		/////////////////////////////////////
		m_srcToTargetTable = new ValueSetMapPanel(srcToTargetMap);
		m_srcToTargetTable.addOpenBtnListener(m_importBtnListener);
		m_srcToTargetTable.addPropertyChangeListener(m_tableListener);

		// other table is reversed
		if (m_biDirectional) {
			m_targetToSrcTable = new ValueSetMapPanel(targetToSrcMap);
			m_targetToSrcTable.addOpenBtnListener(m_importBtnListener);
			m_targetToSrcTable.addPropertyChangeListener(m_tableListener);

			// add tables to left and right sides
			m_tableSplitter.setLeftComponent(m_srcToTargetTable);
			m_tableSplitter.setRightComponent(m_targetToSrcTable);
		} else {
			// add table to center
			m_mainPanel.add(m_srcToTargetTable, BorderLayout.CENTER);
		}

		// save the value sets
		m_srcValueSet = srcValueSet;
		m_targetValueSet = targetValueSet;
		
		setDirty(false);	// disable OK
	}
	
	@Override
	public void dispose() {
		if (m_srcToTargetTable != null) {
			m_srcToTargetTable.removeOpenBtnListener(m_importBtnListener);
			m_srcToTargetTable.removePropertyChangeListener(m_tableListener);
		}
		if (m_targetToSrcTable != null) {
			m_targetToSrcTable.removeOpenBtnListener(m_importBtnListener);
			m_targetToSrcTable.removePropertyChangeListener(m_tableListener);
		}
		super.dispose();
	}
	
	/** Load mapping from a CSV file. The file will be in the form:
	 *        From_Code, From_Description, To_Code, To_Description
	 * @throws IOException 
	 */
	private void importFromCSV(ValueSetMapPanel mappingPanel) throws IOException {
		
		if (mappingPanel == null) {
			return;
		}
		
		boolean imported = mappingPanel.importFromCSV();
		if (imported) {
			
			// update the other table
			ValueSetMapPanel otherPanel = null;
			if (mappingPanel == m_srcToTargetTable) {
				otherPanel = m_targetToSrcTable;
			} else {
				otherPanel = m_srcToTargetTable;
			}
			
			if (otherPanel != null) {
				otherPanel.resetTable();
			}

			mappingPanel.repaint();

			setDirty(true);	// allow OK button
		}
	}
	
	
	//  Value Set
	//  Mappings between A and B
	//  =============================================================================
	//  Mapping from  A to B          [CSV]  ||  Mapping from B to A            [CSV]
	//  -----------------------------------  ||  -----------------------------------
	// | Code | Descr | To Code | To Descr | || | Code | Descr | To Code | To Descr | 
	// |------|-------|---------|----------| || |------|-------|---------|----------|
	// |______|_______|_________|__________| || |______|_______|_________|__________| 
	// |______|_______|_________|__________| || |______|_______|_________|__________| 
	// |______|_______|_________|__________| || |______|_______|_________|__________| 
	//                                       || |______|_______|_________|__________| 
	//
	//
	void buildUI() {
		// create the main parts (top, bottom)
		if (m_biDirectional) {
			JSplitPane splitPane =  new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			m_mainPanel = splitPane;
			m_tableSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

			JPanel topPart = new JPanel();
			splitPane.setTopComponent(topPart);
			splitPane.setBottomComponent(m_tableSplitter);


			// Top
			buildTopPanel(topPart);
			Dimension pref = topPart.getPreferredSize();
			splitPane.setDividerLocation(pref.height + Standards.BOTTOM_INSET + Standards.TOP_INSET);

			// Bottom
			m_tableSplitter.setDividerLocation(500);
			
		} else {
			// just the map
			m_mainPanel = new JPanel(new BorderLayout());
		}
		
		add(m_mainPanel, BorderLayout.CENTER);
	}

	
	void buildTopPanel(JPanel topPanel) {
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.insets = Standards.getInsets();

		//  Mappings between "X" and "Y"
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.weightx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(m_mappingsLabel, gbc);
	}


	@Override
	public boolean isDataValid() {
		return true;
	}

	@Override
	protected void okButtonAction() {
		//update the value sets in the handler with the ones from the map
		m_handler.addValueSet(m_srcValueSet);
		m_handler.addValueSet(m_targetValueSet);
		
		// save file
    	String mapFileName = SystemContext.getMapFileName();
		String handlerFile = mapFileName.replace(".xmi", MdmiValueSetsHandler.FILE_EXTENSION);
		File f = new File(handlerFile);
		m_handler.save(f);

		super.okButtonAction();
	}
	
	
	/** Listener for Import button */
	private class ImportButtonListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			ValueSetMapPanel mappingPanel = null;
			if (e.getSource() ==  m_srcToTargetTable.getImportButton()) {
				mappingPanel = m_srcToTargetTable;
			} else if (e.getSource() == m_targetToSrcTable.getImportButton()) {
				mappingPanel = m_targetToSrcTable;
			}
			
			try {
				importFromCSV(mappingPanel);
			} catch (Exception ex) {
				ExceptionDetailsDialog.showException(ValueSetMapEditor.this, ex);
			}
		}

	}
	
	/** Listener for property change */
	private class TableDataListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() instanceof ValueSetMapPanel &&
					(ValueSetMapPanel.SOURCE_DESCRIPTION.equals(evt.getPropertyName())
							|| ValueSetMapPanel.TARGET_CODE.equals(evt.getPropertyName())
							|| ValueSetMapPanel.TARGET_DESCRIPTION.equals(evt.getPropertyName()))
					) {
				setDirty(true);
			}
		}
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			String lAndF = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lAndF);
		} catch (Exception e) {
		}
		
		JFrame frame = new JFrame();
		frame.setVisible(true);
		
		MdmiValueSetsHandler handler = ValueSetIdentifierDialog.createDummyHandler();
		// find two value sets with maps to/from each other
		Collection<MdmiValueSet> allValueSets = handler.getAllValueSets();
		
		boolean mapsFound = false;
		MdmiValueSetMap map1 = null;
		MdmiValueSetMap map2 = null;

		MdmiValueSet vs1 = null;
		// get first one
		for (MdmiValueSet vs : allValueSets) {
			vs1 = vs;
			// get second one
			for (MdmiValueSet vs2 : allValueSets) {
				if (vs1 == vs2) {
					continue;
				}

				// check for maps in both directions
				map1 = handler.getValueSetMap(vs1.getName() + "." + vs2.getName());
				map2 = handler.getValueSetMap(vs2.getName() + "." + vs1.getName());

				if (map1 != null && map2 != null) {
					// found maps between both
					mapsFound = true;
					break;
				}
			}

			if (mapsFound) {
				// found maps between both
				break;
			}
		}

		if (mapsFound) {
			ValueSetMapEditor testDialog = new ValueSetMapEditor(frame, true, handler, 
					map1.getSourceSet().getName(), map1.getTargetSet().getName());

			// show it
			testDialog.center();
			@SuppressWarnings("unused")
			int val = testDialog.display();
		} else {
			JOptionPane.showMessageDialog(frame, "No maps found");
		}
		
		System.exit(0);

	}

}
