package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.Frame;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.ws.rs.core.UriBuilder;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.CursorManager;
import org.openhealthtools.mdht.mdmi.model.MdmiBusinessElementReference;
import org.openhealthtools.mdht.mdmi.model.MdmiDomainDictionaryReference;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.service.MdmiBusinessElementProxy;
import org.openhealthtools.mdht.mdmi.service.MdmiDatatypeProxy;

/** A utility for accessing the Referent Index Web Service */
public class WebServiceUtillity {


	/**
	 * Import just the data dictionary from the web service
	 */
	public static void importDataDictionaryFromWebService() {
		Frame applicationFrame = SystemContext.getApplicationFrame();
		// set cursor
		CursorManager cm = CursorManager.getInstance(applicationFrame);
		cm.setWaitCursor();
		try {
			// TODO: Prompt for name
			Integer [] names = new Integer[] { 0, 1, 2, 3 };
			Integer nameIdx = (Integer)JOptionPane.showInputDialog(applicationFrame, "Please Select a Name", "Business Element Reference",
					JOptionPane.OK_CANCEL_OPTION, null, names, 0);
			
			if (nameIdx == null) {
				return;
			}

			Collection<MdmiBusinessElementReference> importedElements = getAllBusinessElements(nameIdx.intValue());
			
			if (importedElements == null || importedElements.isEmpty()) {
				JOptionPane.showMessageDialog(applicationFrame, "There are no Business Element References found");
			}

			// do the update
			if (importedElements != null) {
				// update tree - overwrite and warn if reference exists
				ModelIOUtilities.addBusinessElementsToTree(importedElements, true, true);
			}
		} finally {
			cm.restoreCursor();
		}
	}


	/** read all business elements from the service */
	public static Collection<MdmiBusinessElementReference> getAllBusinessElements(int nameIndex) {
		Collection<MdmiBusinessElementReference> allElements = new ArrayList<MdmiBusinessElementReference>();

		// Get the proxy items for the service
		MdmiDatatypeProxy pdt = new MdmiDatatypeProxy(getBaseUri(), getToken());
		MdmiBusinessElementProxy pbe = new MdmiBusinessElementProxy(getBaseUri(), getToken(), pdt);

		// create a temporary message group
		MessageGroup tempGroup = new MessageGroup();
		tempGroup.setName("Temporary Group");
		tempGroup.setDomainDictionary(new MdmiDomainDictionaryReference());

		// Start reading
		boolean allDataRead = false;
		int idx = 0;
		while (!allDataRead) {
			// read 100 at a time
			MdmiBusinessElementReference[] besRead = pbe.getAll(idx, tempGroup, nameIndex);
			for( MdmiBusinessElementReference beRead : besRead ) {
				allElements.add(beRead);
			}

			// adjust for next 100
			if (besRead.length > 0) {
				idx += besRead.length;
			} else {
				allDataRead = true;
			}
		}
		return allElements;
	}


	private static URI getBaseUri() {
		return UriBuilder.fromUri("http://52.1.151.176:8080/MdmiSvc").build(); // 107.22.213.68
	}

	private static String getToken() {
		return "KenLord-MDMI2013";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// show all BERs
		Collection<MdmiBusinessElementReference> allElements = getAllBusinessElements(0);
		for (MdmiBusinessElementReference ber : allElements) {
			System.out.println("Got " + ber.getName() + ", UID=" + ber.getUniqueIdentifier());
		}
	}
}
