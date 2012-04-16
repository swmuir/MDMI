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

import java.util.*;

import org.openhealthtools.mdht.mdmi.model.*;

/**
 * One transformation set - a collection of Message Elements to transfer from one message to the other, together with
 * the map references, and messages.
 */
public class MdmiTransferInfo {
   /** The source model describing the source message. */
   public MdmiModelRef      sourceModel;
   /** The source message. */
   public MdmiMessage       sourceMessage;
   /** The target model describing the target message. */
   public MdmiModelRef      targetModel;
   /** The target message. */
   public MdmiMessage       targetMessage;
   /** A list of target element names to be transfered. May be Semantic Element names or Business Element names, depending upon the useDictionary. */
   public ArrayList<String> targetElements;
   /** If true, the names in the targetElements are Semantic Element names, otherwise they are Business Element names.*/
   public boolean           useDictionary;

   /**
    * Construct a new transfer info instance. By default the useDictionary is false, meaning that the 
    * target element names are Semantic Element names.
    * 
    * @param sourceModel The source model describing the source message.
    * @param sourceMessage The source message.
    * @param targetModel The target model describing the target message.
    * @param targetMessage The target message.
    * @param targetElements The target element names to be transfered.
    */
   public MdmiTransferInfo( MdmiModelRef sourceModel, MdmiMessage sourceMessage, MdmiModelRef targetModel,
         MdmiMessage targetMessage, ArrayList<String> targetElements ) {
      this.sourceModel = sourceModel;
      this.sourceMessage = sourceMessage;
      this.targetModel = targetModel;
      this.targetMessage = targetMessage;
      this.targetElements = targetElements;
   }

   /**
    * Copy ctor.
    * 
    * @param src Source of the copy.
    */
   public MdmiTransferInfo( MdmiTransferInfo src ) {
      this.sourceModel = src.sourceModel;
      this.sourceMessage = src.sourceMessage;
      this.targetModel = src.targetModel;
      this.targetMessage = src.targetMessage;
      this.targetElements = src.targetElements;
   }

   /**
    * Get the source message group.
    * 
    * @return The source message group.
    */
   public MessageGroup getSourceMessageGroup() {
      return sourceModel.getGroup();
   }

   /**
    * Get the target message group.
    * 
    * @return The target message group.
    */
   public MessageGroup getTargetMessageGroup() {
      return targetModel.getGroup();
   }

   @Override
   public String toString() {
      try {
         StringBuffer sbe = new StringBuffer();
         for( int i = 0; i < targetElements.size(); i++ ) {
            if( i > 0 )
               sbe.append(", ");
            sbe.append(targetElements.get(i));
         }
         return "TRANFORM FROM: " + sourceModel.toString() + "[" + sourceMessage.toString() + "]\r\n" + "TO: "
               + targetModel.toString() + "[" + targetMessage.toString() + "]\r\n" + "ELEMENTS: " + sbe.toString();
      }
      catch( Exception ignored ) {}
      return super.toString();
   }

} // MdmiTransformInfo

