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
package org.openhealthtools.mdht.mdmi.model.syntax;

import java.util.List;

import org.openhealthtools.mdht.mdmi.model.Node;

public class XSDtoCSV {
	public static void main( String[] args ) {
		if (args.length != 2) {
			System.out.println("Usage: XSDtoCSV <input XSD> <output CSV>");
			return;
		}
		
		List<Node> roots = XSDReader.parse("file://localhost/" + args[0]);

		toCSV(args[1],roots.get(0));
	}

	/**
    * toCSV - takes a root MDMI node object and writes it out into a blank CSV file.
    * 
    * @param path	The path of the output CSV file
    * @param root	The root MDMI node object that will be attached to the resulting Message Model.
    */
	public static void toCSV(String path, Node root) {
	}
}
