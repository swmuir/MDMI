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
package org.openhealthtools.mdht.mdmi.model.csv.reader;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class CsvReader {
	private File p_file = null;
	private FileReader p_fstream = null;
	private BufferedReader p_bstream = null;
	private String[] s_columns = null;
	private String[] s_headers = null;
	private Map<String,Integer> p_map = null;
	private String s_delimiter = ",";
	
	public CsvReader(String path) throws IOException {
		if (path == null)
			throw new IllegalArgumentException("Erro: no file specified.");
		
		p_file = new File(path);
		
		if (!p_file.exists())
			throw new FileNotFoundException("Error: the specified file does not exist.");
		else if (!p_file.canRead())
			throw new IOException("Error: unable to read the specified file.");
		
		p_fstream = new FileReader(path);
		p_bstream = new BufferedReader(p_fstream);
	}
	
	public boolean readRecord() throws IOException {
		String buffer = p_bstream.readLine();
		
		if (buffer == null)
			return false;

		// split the 
		s_columns = buffer.split(s_delimiter);
		
		return true;
	}

	public int getColumnCount() {
		if (s_columns == null)
			return 0;
		
		return s_columns.length;
	}
	
	public String get(String columnName) {
		if (s_headers == null || p_map == null || !p_map.containsValue(columnName))
			return "";
		
		return get(p_map.get(columnName));
	}
	
	public String get(int columnIndex) {
		if (s_columns == null || columnIndex < 0 || columnIndex >= s_columns.length)
			return "";
		
		return s_columns[columnIndex];
	}

	public boolean readHeaders() throws IOException {
		// read first record
		if (!readRecord())
			return false;
		
		// clone the column array
		s_headers = s_columns.clone();

		// create dictionary
		p_map = new HashMap<String,Integer>();
		for (int i=0; i < s_headers.length; i++)
			p_map.put(s_headers[i], i);
		
		return true;
	}
	
	public int getHeaderCount() {
		if (s_headers == null)
			return 0;
		
		return s_headers.length;
	}

	public String getHeader(int ndx) {
		if (s_headers == null || ndx < 0 || ndx >= s_headers.length)
			return "";
		
		return s_headers[ndx];
	}
}
