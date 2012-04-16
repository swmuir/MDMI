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
 *     Wency Chingcuangco
 *
 *******************************************************************************/
package org.openhealthtools.mdht.mdmi.engine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nrl.parser.NRLError;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.Util;

public class NrlAdapter implements IExpressionInterpreter {
	IExpressionInterpreter m_adapter = null;

	public NrlAdapter() {
		try {
			m_adapter = Util.getInstance("com.whitestar.mdmi.NrlAdapter", new File("nrl-adapter.jar"), null, null);
		}
		catch (Exception ex) {
		}
	}

	public NrlAdapter(ElementValueSet eset, XElementValue context,	String name, XValue value) {
		try {
			m_adapter = Util.getInstance("com.whitestar.mdmi.NrlAdapter", new File("nrl-adapter.jar"), null, null);
			initialize (eset, context, name, value);
		}
		catch (Exception ex) {
		}
	}
	
	public void initialize(ElementValueSet eset, XElementValue context,	String name, XValue value) {
		if (m_adapter != null)
			m_adapter.initialize(eset, context, name, value);
	}

	public boolean evalConstraint(IElementValue context, String rule) {
		if (m_adapter != null && !rule.isEmpty())
			return m_adapter.evalConstraint(context, rule);
		else if (!rule.isEmpty())
			throw new MdmiException("The open source version does not support the use of NRL rules.");

		return true;
	}

	public void evalAction(IElementValue context, String rule) {
		if (m_adapter != null && !rule.isEmpty())
			m_adapter.evalAction(context, rule);
		else if (!rule.isEmpty())
			throw new MdmiException("The open source version does not support the use of NRL rules.");
	}
	
	public List<NRLError> compileConstraint( SemanticElement se, String rule ) {
		if (m_adapter != null && !rule.isEmpty())
			return m_adapter.compileConstraint(se, rule);
		else if (!rule.isEmpty())
			throw new MdmiException("The open source version does not support the use of NRL rules.");
		
		return new ArrayList<NRLError>();
	}

	public List<NRLError> compileAction( SemanticElement se, String rule ) {
		if (m_adapter != null && !rule.isEmpty())
			return m_adapter.compileAction(se, rule);
		else if (!rule.isEmpty())
			throw new MdmiException("The open source version does not support the use of NRL rules.");

		return new ArrayList<NRLError>();
	}
} // NrlAdapter
