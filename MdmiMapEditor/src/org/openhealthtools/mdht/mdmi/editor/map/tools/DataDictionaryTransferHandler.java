/*******************************************************************************
 * Copyright (c) 2013 Firestar Software, Inc.
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

package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;
import org.openhealthtools.mdht.mdmi.editor.common.components.ExceptionDetailsDialog;
import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.validate.ModelValidationResults;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.reader.MapBuilderXMIDirect;


/** Drop support for copying Business Element References from another source */
public class DataDictionaryTransferHandler extends TransferHandler {

	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		// we only import Strings
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}

		// Check for String flavor
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}

		// read it
	    Transferable t = info.getTransferable();
	    String data;
	    try {
			data = (String) t.getTransferData(DataFlavor.stringFlavor);
			
			// try to import it
			ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
			ModelValidationResults results = new ModelValidationResults();

            List<MessageGroup> newGroups = MapBuilderXMIDirect.build(is, results);
            if (newGroups != null) {
                // update tree - overwrite and warn if reference exists
                ModelIOUtilities.addImportedBusinessElementRefToTree(newGroups, true, true);
            }
			
			
		} catch (Exception e) {
			ExceptionDetailsDialog.showException(SystemContext.getApplicationFrame(), e);
		}


		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

}