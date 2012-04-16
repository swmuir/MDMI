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

import org.openhealthtools.mdht.mdmi.MdmiConfig.PreProcessorInfo;
import org.openhealthtools.mdht.mdmi.util.*;

/**
 * Collection of pre processors registered with this runtime. Will be loaded from the config file at the start, and may
 * be updated later.
 */
public final class MdmiPreProcessors {
   private final ArrayList<IPreProcessor> m_preProcessors = new ArrayList<IPreProcessor>();

   MdmiPreProcessors( MdmiConfig config ) {
      loadProcessors(config);
   }

   /**
    * Load the providers specified in the config file.
    */
   void loadProcessors( MdmiConfig config ) {
      Collection<PreProcessorInfo> infos = config.getAllPreProcessorInfos();
      if( 0 <= infos.size() ) {
         for( Iterator<PreProcessorInfo> iterator = infos.iterator(); iterator.hasNext(); ) {
            PreProcessorInfo info = iterator.next();
            IPreProcessor obj = Util.getInstance(info.className, new File(info.jarFileName), null, null);
            System.out.println("Loaded Pre-Processor: " + info.className + " in " + info.jarFileName);
            m_preProcessors.add(obj);
         }
      }
   }

   /**
    * From the list of pre-processors call all the ones that are registered to handle the source or target messages,
    * based on the message group name and message model name.
    * 
    * @param transferInfo The transfer info which is just about to being processed by the runtime.
    */
   public void preProcess( MdmiTransferInfo transferInfo ) {
      if( transferInfo == null )
         throw new IllegalArgumentException("transferInfo is null");
      for( int i = 0; i < m_preProcessors.size(); i++ ) {
         IPreProcessor p = m_preProcessors.get(i);
         if( p.canProcess(transferInfo.sourceModel.getGroupName(), transferInfo.sourceModel.getModelName()) ) {
            Mdmi.INSTANCE.logger().info("Pre-processor {0} called for source model {1}", p.getName(),
                  transferInfo.sourceModel.getQualifiedName());
            try {
               p.processMessage(transferInfo.sourceMessage, transferInfo.sourceModel, true);
            }
            catch( Exception ex ) {
               throw new MdmiException(
                     ex,
                     "MdmiPreProcessor {0} throws an unexpected exception while processing source of transfer request {1}",
                     p.getName(), transferInfo.toString());
            }
         }
         if( p.canProcess(transferInfo.targetModel.getGroupName(), transferInfo.targetModel.getModelName()) ) {
            Mdmi.INSTANCE.logger().info("Pre-processor {0} called for target model {1}", p.getName(),
                  transferInfo.targetModel.getQualifiedName());
            try {
               p.processMessage(transferInfo.targetMessage, transferInfo.targetModel, false);
            }
            catch( Exception ex ) {
               throw new MdmiException(
                     ex,
                     "MdmiPreProcessor {0} throws an unexpected exception while processing target of transfer request {1}",
                     p.getName(), transferInfo.toString());
            }
         }
      }
   }
} // MdmiPreProcessors
