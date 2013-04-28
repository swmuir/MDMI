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

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.mdmi.model.MessageGroup;
import org.openhealthtools.mdht.mdmi.model.MessageModel;
import org.openhealthtools.mdht.mdmi.model.MessageSyntaxModel;
import org.openhealthtools.mdht.mdmi.model.Node;
import org.openhealthtools.mdht.mdmi.model.xmi.direct.writer.XMIWriterDirect;

public class XSDtoXMI {
	public static void main( String[] args ) {
		if (args.length != 2) {
			System.out.println("Usage: XSDtoXMI <input XSD> <output XMI>");
			return;
		}
		
		List<Node> roots = XSDReader.parse("file://localhost/" + args[0]);

		toXMI(args[1],roots.get(0));
	}
	
   /**
    * toXMI - takes a root MDMI node object and writes it out into a blank MDMI map.
    * 
    * @param path	The path of the output MDMI map.
    * @param root	The root MDMI node object that will be attached to the resulting Message Model.
    */
	public static void toXMI(String path, Node root) {
		List<MessageGroup> messageGroups = new ArrayList<MessageGroup>();
		MessageGroup messageGroup = new MessageGroup();
		MessageModel messageModel = new MessageModel();
		MessageSyntaxModel syntaxModel = new MessageSyntaxModel();

		syntaxModel.setRoot(root);
		messageModel.setSyntaxModel(syntaxModel);
		messageGroup.addModel(messageModel);
		messageGroups.add(messageGroup);
		
		try {
			XMIWriterDirect.write(path,messageGroups);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
