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

import java.io.*;
import java.util.*;
import org.openhealthtools.mdht.mdmi.MdmiConfig.ExternalResolverInfo;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;

/**
 * Collection of external resolvers for resolving external data types.
 * Will be loaded from the config file at the start, and may be updated later.
 */
public final class MdmiExternalResolvers {
   private final ArrayList<IExternalResolver> m_resolvers = new ArrayList<IExternalResolver>();
   private final HashMap<DTExternal,IExternalResolver> m_resolversMap = new HashMap<DTExternal,IExternalResolver>();

   MdmiExternalResolvers( MdmiConfig config ) {
      loadResolvers(config);
   }

   /**
    * Load the providers specified in the config file.
    */
   void loadResolvers( MdmiConfig config ) {
      Collection<ExternalResolverInfo> infos = config.getAllResolverInfos();
      if( 0 <= infos.size() ) {
         for( Iterator<ExternalResolverInfo> iterator = infos.iterator(); iterator.hasNext(); ) {
            ExternalResolverInfo info = iterator.next();
            IExternalResolver obj = Util.getInstance(info.className, new File(info.jarFileName), null, null);
            System.out.println("Loaded External Resolver: " + info.className + " in " + info.jarFileName);
            m_resolvers.add(obj);
         }
      }
   }
   
    private void setResolvers(DTExternal datatype){
   	  for( IExternalResolver resolver : m_resolvers ) {         
           if( resolver.canHandle(datatype) ) {
              m_resolversMap.put(datatype,resolver);
              return;
           }
        }
   	  Mdmi.INSTANCE.logger().warning("A resolver was not found for data type: " + datatype.getName());
   	  m_resolversMap.put(datatype,null);
   	
   }
   
   /**
    * Find a resolver that can handle the given datatype and value, and call its getValue()
    * method and return the value. If none found will return the given value.
    * This method return the dictionary value when the model-specific value is given. 
    *  
    * @param datatype The external datatype. 
    * @param value The actual value.
    * @return The external value, if a resolver is found, the original value if none was found.
    */
   public Object getDictionaryValue( DTExternal datatype, String value ) {
      if( datatype == null )
         throw new IllegalArgumentException("Datatype is null");
      
      if (!m_resolversMap.containsKey(datatype)) {
      	 setResolvers( datatype);
      }
     
         IExternalResolver p =m_resolversMap.get(datatype);
         if( p != null ) {
            return p.getDictionaryValue(datatype, value);
         }
   
  
      return value;
   }
   
   /**
    * Find a resolver that can handle the given datatype and value, and call its getValue()
    * method and return the value. If none found will return the given value.
    * This method return the model-specific value when the dictionary value is given. 
    *  
    * @param datatype The external datatype. 
    * @param value The actual value.
    * @return The external value, if a resolver is found, the original value if none was found.
    */
   public String getModelValue( DTExternal datatype, Object value ) {
      if( datatype == null )
         throw new IllegalArgumentException("Datatype is null");
     
      if (!m_resolversMap.containsKey(datatype)) {
     	 setResolvers( datatype);
     }
    
        IExternalResolver p =m_resolversMap.get(datatype);
        if( p != null ) {
            return p.getModelValue(datatype, value);
        }
   
      return value == null ? null : value.toString();
   }
} // MdmiExternalResolvers
