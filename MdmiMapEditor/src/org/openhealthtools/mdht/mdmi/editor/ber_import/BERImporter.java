package org.openhealthtools.mdht.mdmi.editor.ber_import;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.xml.stream.XMLStreamException;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;
import org.openhealthtools.mdht.mdmi.editor.map.MapEditor;
import org.openhealthtools.mdht.mdmi.editor.map.editor.TextSearcher;
import org.openhealthtools.mdht.mdmi.editor.map.tools.Comparators;
import org.openhealthtools.mdht.mdmi.editor.map.tools.ModelIOUtilities;
import org.openhealthtools.mdht.mdmi.model.DTComplex;
import org.openhealthtools.mdht.mdmi.model.DTSDerived;
import org.openhealthtools.mdht.mdmi.model.Field;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDatatype;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;
import org.openhealthtools.mdht.mdmi.util.LogWriter;

/**
 * This class will serve as a prototype for a tool to import
 * Business Element Reference objects received via a service, and 
 * subsequently imported into the Map Editor.
 * 
 * For prototype purposes, this tool will read the data from an XMI file
 * 
 * @author Sally Conway
 *
 */
public class BERImporter extends JFrame {

	// model
	private List<MdmiBusinessElementReference> m_businessElements;
	
	// view
	private JList m_mainList;
	private JTextField m_filterText;
	private JRadioButton m_berNameButton;
	private JRadioButton m_datatypeNameButton;
	private JButton m_filterButton;
	

    public BERImporter() throws HeadlessException {
		super();

		SystemContext.setApplicationName("BERImporter");
		SystemContext.setApplicationFrame(this);
		
		// simple exit handling
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// build components
		buildUI();
	}

	
    // Build the UI 
	private void buildUI() {
		setTitle("Business Element References Utility");
		
		getContentPane().setLayout(new BorderLayout());
		
		m_businessElements = new ArrayList<MdmiBusinessElementReference>();
		m_mainList = new JList(new DefaultListModel());
		m_mainList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		m_mainList.setDragEnabled(true);
		m_mainList.setTransferHandler(new ListTransferHandler());
		m_mainList.setCellRenderer(new ListCellRenderer());

		JScrollPane listScroller = new JScrollPane(m_mainList);
		listScroller.setPreferredSize(new Dimension(600, 500));
		
		getContentPane().add(listScroller, BorderLayout.CENTER);
		

		// Filtering
		m_filterText = new JTextField(20);
		m_filterButton = new JButton(new AbstractAction("Go") {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayBERs();
			}
		});

		m_berNameButton = new JRadioButton("Match BER Name", true);
		m_datatypeNameButton = new JRadioButton("Match Datatype Name", false);
		ButtonGroup group = new ButtonGroup();
		group.add(m_berNameButton);
		group.add(m_datatypeNameButton);
		
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(new JLabel("Enter Search String: "));
		filterPanel.add(m_filterText);
		filterPanel.add(m_berNameButton);
		filterPanel.add(m_datatypeNameButton);
		filterPanel.add(m_filterButton);
		getContentPane().add(filterPanel, BorderLayout.NORTH);
		
	}

	// save this datatype, and all associated datatypes, in the list (if it's not there already)
	private void saveReferentDatatypes(ArrayList<MdmiDatatype> neededDatatypes, MdmiDatatype datatype) {
		if (datatype == null) {
			return;
		}

		// check first
		if (!neededDatatypes.contains(datatype)) {
			// add it
			neededDatatypes.add(datatype);
			
			// Check on fields, etc.
			if (datatype instanceof DTComplex) {
				DTComplex complexType = (DTComplex)datatype;
				for (Field field : complexType.getFields()) {
					if (field.getDatatype() != null) {
						saveReferentDatatypes(neededDatatypes, field.getDatatype());
					}
				}
            } else if (datatype instanceof DTSDerived) {
            	DTSDerived derType = (DTSDerived)datatype;
            	saveReferentDatatypes(neededDatatypes, derType.getBaseType());
            }
		}
	}


	// add a BER to the display
	public void addBER(MdmiBusinessElementReference ber) {
		m_businessElements.add(ber);
	}
	
	// display the BERs
	public void displayBERs()
	{
		DefaultListModel model = (DefaultListModel)m_mainList.getModel();
		model.removeAllElements();
		
		// no filtering
		String filterText = m_filterText.getText().trim();
		if (filterText == null || filterText.isEmpty()) {
			for (MdmiBusinessElementReference ber : m_businessElements) {
				model.addElement(ber);
			}
			
		} else {
			int flags = Pattern.CASE_INSENSITIVE;
			String regExString = TextSearcher.toRegex(filterText);
			boolean matchBER = m_berNameButton.isSelected();
			try {
				Pattern pattern = Pattern.compile(regExString, flags);
				String nameToMatch;
				for (MdmiBusinessElementReference ber : m_businessElements) {
					// add if it matches
					if (matchBER) {
						nameToMatch = ber.getName();
					} else if (ber.getReferenceDatatype() != null) {
						nameToMatch = ber.getReferenceDatatype().getName();
					} else {
						nameToMatch = "";
					}
					if (pattern.matcher(nameToMatch).matches()) {
						model.addElement(ber);
					}
				}
			} catch (Exception ex) {
				ExceptionDetailsDialog.showException(this, ex);
			}
		}
		
		if (model.isEmpty()) {
			model.addElement("No Data Found");
		}
		
		m_mainList.invalidate();
	}
	
	// List renderer
	private class ListCellRenderer extends DefaultListCellRenderer {


		private static final String HTML_SPACE = "&nbsp;&nbsp;";

		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			MdmiBusinessElementReference ber = null;
			if (value instanceof MdmiBusinessElementReference) {
				ber = (MdmiBusinessElementReference)value;
				
				StringBuilder buf = new StringBuilder("<html>");
				// 1. Name (datatype)
				// line number
				int lineNo = index+1;
				// insert spaces
				if (lineNo < 1000) buf.append(HTML_SPACE);
				if (lineNo < 100) buf.append(HTML_SPACE);
				if (lineNo < 10) buf.append(HTML_SPACE);
				buf.append(lineNo).append(".").append(HTML_SPACE);
				// name
				buf.append(ber.getName());
				
				// datatype in dark blue, italics
				buf.append(" (").append("<font color=\"#0000A0\"><i>");
				if (ber.getReferenceDatatype() != null) {
					buf.append(ber.getReferenceDatatype().getName());
				} else {
					buf.append("- no datatype -");
				}
				buf.append("</i></font>").append(")");
				
				buf.append("</html>");
				value = buf.toString();
			}
			return super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
		}
	}
	
	// Transfer from List
	private class ListTransferHandler extends TransferHandler {
		@Override
		public int getSourceActions(JComponent c) {
			return COPY;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			JList list = (JList)c;
			Object[] values = list.getSelectedValues();
			
			// we need these groups
			ArrayList<MessageGroup> neededGroups = new ArrayList<MessageGroup>();
			ArrayList<MdmiBusinessElementReference> neededBERs = new ArrayList<MdmiBusinessElementReference>();
			ArrayList<MdmiDatatype> neededDatatypes = new ArrayList<MdmiDatatype>();
			
			for (int i=0; i<values.length; i++) {
				Object value = values[i];
				
				if (value instanceof MdmiBusinessElementReference) {
					MdmiBusinessElementReference ber = (MdmiBusinessElementReference)value;
					// identify message group
					MessageGroup group = ber.getDomainDictionaryReference().getMessageGroup();
					if (!neededGroups.contains(group)) {
						neededGroups.add(group);
					}
					
					// Save it
					neededBERs.add(ber);
					
					// check on datatype
					if (ber.getReferenceDatatype() != null) {
						MdmiDatatype datatype = ber.getReferenceDatatype();
						saveReferentDatatypes(neededDatatypes, datatype);
					}
				}
			}
			
			// make a copy of the full list of BERs and Data Types
			HashMap<MessageGroup, ArrayList<MdmiBusinessElementReference>> savedBERsMap = 
					new HashMap<MessageGroup, ArrayList<MdmiBusinessElementReference>>();
			HashMap<MessageGroup, ArrayList<MdmiDatatype>> savedDatatypesMap = 
					new HashMap<MessageGroup, ArrayList<MdmiDatatype>>();

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			for (MessageGroup group : neededGroups) {
				Collection<MdmiDatatype> datatypes = group.getDatatypes();
				// save the old datatypes
				ArrayList<MdmiDatatype> savedDatatypes = new ArrayList<MdmiDatatype>();
				savedDatatypes.addAll(datatypes);
				savedDatatypesMap.put(group, savedDatatypes);

				// save the old BERs
				MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
				ArrayList<MdmiBusinessElementReference> savedBERs = new ArrayList<MdmiBusinessElementReference>();
				savedBERs.addAll(dictionary.getBusinessElements());
				savedBERsMap.put(group, savedBERs);

				// Replace datatypes and BERs with the selected ones
				datatypes.clear();
				dictionary.getBusinessElements().clear();

				// Add back the selected BERs
				for (MdmiBusinessElementReference ber : neededBERs) {
					if (ber.getDomainDictionaryReference() == dictionary) {
						dictionary.addBusinessElement(ber);
					}
				}

				// Add the required data types
				for (MdmiDatatype datatype : neededDatatypes) {
					if (datatype.getOwner() == group || datatype.isPrimitive()) {
						datatypes.add(datatype);
					}
				}

			}
			
			// convert to stream
			try {
				XMIWriterDirect.write(os, neededGroups);
			} catch (XMLStreamException ex) {
				ExceptionDetailsDialog.showException(BERImporter.this, ex);
			}
			
			// restore BERs and Datatypes
			for (MessageGroup group : neededGroups) {
				// restore the old datatypes
				Collection<MdmiDatatype> datatypes = group.getDatatypes();
				datatypes.clear();
				ArrayList<MdmiDatatype> savedDatatypes = savedDatatypesMap.get(group);
				datatypes.addAll(savedDatatypes);

				// restore the old BERs
				MdmiDomainDictionaryReference dictionary = group.getDomainDictionary();
				dictionary.getBusinessElements().clear();
				ArrayList<MdmiBusinessElementReference> savedBERs = savedBERsMap.get(group);
				dictionary.getBusinessElements().addAll(savedBERs);
			}

			return new StringSelection(os.toString());
		}
	}

	/**
     * Starting point for application.
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

        final BERImporter importer =  new BERImporter();
        importer.pack();
        importer.setLocation(100,100);
        importer.setVisible(true);
        
        // prompt for input
        ArrayList<MessageGroup> groups = ModelIOUtilities.promptAndReadModel(ModelIOUtilities.LAST_FILE_IMPORTED);
        if (groups != null) {
        	for (MessageGroup group : groups) {
        		// wipe out stuff we dont need
        		group.getModels().clear();
				group.getDataRules().clear();
        		
        		// list all BERs
        		ArrayList<MdmiBusinessElementReference> elements = new ArrayList<MdmiBusinessElementReference>();
        		elements.addAll(group.getDomainDictionary().getBusinessElements());

        		// sort by name
        		Collections.sort(elements, new Comparators.BusinessElementReferenceComparator());

        		for (MdmiBusinessElementReference ber : elements) {
        			importer.addBER(ber);
        		}
        	}
        }
        
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
		        importer.displayBERs();
			}
		});
        
    }
}
