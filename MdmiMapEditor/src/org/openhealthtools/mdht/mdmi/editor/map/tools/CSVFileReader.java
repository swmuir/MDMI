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
package org.openhealthtools.mdht.mdmi.editor.map.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CSVFileReader {

	private static final String COMMA = ",";
	private DataInputStream m_in = null;
	private BufferedReader m_br = null;
	
	private String m_token = COMMA;
	
	// create a file reader
	public CSVFileReader(File file) throws FileNotFoundException {
		FileInputStream fstream = new FileInputStream(file);
		
		// Read File Line By Line
		m_in = new DataInputStream(fstream);
		m_br = new BufferedReader(new InputStreamReader(m_in));
	}
	
	/** Define the token used to delimit fields. The default is a comma */
	public void setSeparatorToken(char token) {
		m_token = "" + token;
	}
	
	// get next line, as a list of string
	public List<String> getNextLine() throws IOException {
		String strLine;
		if ((strLine = m_br.readLine()) != null) {
			// comma-separated
			List<String> strings = new ArrayList<String>();
			StringTokenizer strTok = new StringTokenizer(strLine, m_token, true);
			// add each string - treat consecutive commas as a blank
			// A,B,C -> A and B and C
			// A,, -> A and blank and blank
			// ,, -> blank and blank and blank
			boolean prevString = false;
			while (strTok.hasMoreTokens()) {
				String tok = strTok.nextToken().trim();
				if (m_token.equals(tok)) {
					// add a blank for two consecutive commas
					if (!prevString) {
						strings.add(new String());
					}
					prevString = false;
				} else {
					strings.add(tok);
					prevString = true;
				}
			}
			
			return strings;
		}
		return null;
	}
	
	// close when done reading
	public void close() {
		if (m_in != null) {
			try {
				m_in.close();
			} catch (IOException e) {
				// don't care
			}
		}
	}
}
