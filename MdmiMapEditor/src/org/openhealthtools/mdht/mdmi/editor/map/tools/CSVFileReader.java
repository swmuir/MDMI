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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

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


	// check whether each string in the list is empty
	public static boolean isEmptyList(List<String> list) {
		return isEmptyList(list, 0);
	}
	
	// check whether each string in the list is empty, starting with the i'th element
	public static boolean isEmptyList(List<String> list, int idx) {
		boolean isEmpty = true;
		for (int i=idx; i<list.size(); i++) {
			if (list.get(i).length() > 0) {
				isEmpty = false;
				break;		
			}
		}
		return isEmpty;
	}


	
	// get the i'th string in a list. If the list is shorter than 
	// needed, return an empty string
	public static String getString(List<String>list, int idx)
	{
		String s = "";
		if (list != null && idx < list.size()) {
			// strip off leading and trailing quotes if there are any
			s = stripQuotes(list.get(idx));
		}
		return s;
	}

	// if there are leading and trailing quotes, it means there are embedded quotes -
	// e.g. "EncounterPerformerRepresentedOrganizationPhone.use = ""WP"""
	//   --> EncounterPerformerRepresentedOrganizationPhone.use = "WP"
	private static String stripQuotes(String string) {
		int length = string.length();
		if (length > 2 && string.charAt(0) == '"' && string.charAt(length - 1) == '"') {
			StringBuilder newString = new StringBuilder();
			for (int i = 0; i < length; i++) {
				if (i == 0 || i == length - 1) {
					continue; // strip first and last
				}
				
				char c = string.charAt(i);
				if (c == '"') {
					// replace doubles with a single
					if (i == 0 || string.charAt(i - 1) != '"') {
						continue;
					}
				}
				newString.append(c);
			}
			string = newString.toString();
		}
		return string;
	}

	/** Get a file chooser that's configured to pick a CSV file */
	public static JFileChooser getCSVFileChooser(String lastFileName) {

		// create a file chooser
		JFileChooser chooser = new JFileChooser(lastFileName == null ? "." : lastFileName);
		chooser.setFileFilter(new CSVFileReader.CSVFilter());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		return chooser;
	}

	/** A file filter for CSV files */
    public static class CSVFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
        }

        @Override
        public String getDescription() {
            return "CSV Files (.csv)";
        }
    }
}
