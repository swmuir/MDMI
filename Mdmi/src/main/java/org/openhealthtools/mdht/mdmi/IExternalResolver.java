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
 * Used to resolve external data types.
 */
public interface IExternalResolver {
   /**
    * Returns true if the implementor can handle this data type.
    * 
    * @param dataType The external data type that is sought to be resolved.
    * @return True if the implementor can handle this data type.
    */
   public boolean canHandle( DTExternal dataType );
   
   /**
    * Get a list of all data types this resolver knows how to handle.
    * 
    * @return A list of all data types this resolver knows how to handle.
    */
   public ArrayList<String> getHandledDataTypes();

   /**
    * Get the value for an external data type from an outside source, maybe a database or a web service.
    * This method return the dictionary value when the model-specific value is given. 
    * 
    * @param dataType The external data type definition in the model. 
    * @param value The value in the source message.
    * @return The resolved external value.
    */
   public Object getDictionaryValue( DTExternal dataType, String value );

   /**
    * Get the value for an external data type from an outside source, maybe a database or a web service.
    * This method return the model-specific value when the dictionary value is given. 
    * 
    * @param dataType The external data type definition in the model. 
    * @param value The value in the source message.
    * @return The resolved external value.
    */
   public String getModelValue( DTExternal dataType, Object value );
} // IExternalResolver
