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
*     Gabriel Oancea
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi;

import org.openhealthtools.mdht.mdmi.model.*;

/**
 * Implemented by classes that know how to parse and serialize MDMI messages.
 */
public interface ISyntacticParser {
   /**
    * Parse an MDMI message into a token tree (YNode tree).
    * 
    * @param mdl The MessageModel for the specified message.
    * @param msg The actual message (bytes).
    * @return The YNode that represents the root of the tree.
    */
   public ISyntaxNode parse( MessageModel mdl, MdmiMessage msg );

   /**
    * Serialize (write) an MDMI message from the given syntax tree (the YNode is the root).
    * 
    * @param mdl The MessageModel for the specified message.
    * @param msg The actual message where the bytes will be written.
    * @param root The syntax tree.
    */
   public void serialize( MessageModel mdl, MdmiMessage msg, ISyntaxNode root );
} // ISyntaxParser
