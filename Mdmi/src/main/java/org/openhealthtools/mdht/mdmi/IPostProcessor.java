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

import java.util.ArrayList;

/**
 * An interface implemented by a provider that wishes to process the source or target messages after
 * the runtime is done with them. Typically in involves parsing the XML message and adding or removing elements
 * or attributes. 
 * 
 * @author goancea
 */
public interface IPostProcessor {
   /**
    * Get the unique name of this processor.
    * 
    * @return The unique name of this processor.
    */
   public String getName();
   
   /**
    * Get all handled qualified message names, that is formatted as 'groupName.messageName'.
    * 
    * @return All handled qualified message names, that is formatted as 'groupName.messageName'.
    */
   public ArrayList<String> getHandledQualifiedMessageNames();
   
   /**
    * Return true if interested to process the message (can be source or target) for the specified message
    * group and message models. The runtime will call this method for all providers, and only if it returns true
    * it will invoke the process method, otherwise nothing will happen.
    * 
    * @param messageGroupName The message group name.
    * @param messageModelName The message model name.
    * @return True if interested in processing messages of this type.
    */
   public boolean canProcess( String messageGroupName, String messageModelName );
   
   /** 
    * Execute the processing of the specified message, do what is necessary to the message and set the
    * values back as expected by the runtime. Note that the message is always the target message in its 
    * final form.
    * 
    * @param message The message reference.
    * @param model The model reference.
    */
   public void processMessage( MdmiMessage message, MdmiModelRef model );
} // IPostProcessor
