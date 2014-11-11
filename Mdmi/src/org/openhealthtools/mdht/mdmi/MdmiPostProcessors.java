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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openhealthtools.mdht.mdmi.MdmiConfig.ProcessorInfo;
import org.openhealthtools.mdht.mdmi.util.Util;

/**
 * Collection of post processors registered with this runtime.
 * Will be loaded from the config file at the start, and may be updated later.
 */
public final class MdmiPostProcessors {
   private final ArrayList<IPostProcessor> m_postProcessors = new ArrayList<IPostProcessor>();

   MdmiPostProcessors( MdmiConfig config ) {
      loadProcessors(config);
   }

   /**
    * Load the providers specified in the config file.
    */
   void loadProcessors( MdmiConfig config ) {
      Collection<ProcessorInfo> infos = config.getAllPostProcessorInfos();
      if( 0 <= infos.size() ) {
         for( Iterator<ProcessorInfo> iterator = infos.iterator(); iterator.hasNext(); ) {
         	ProcessorInfo info = iterator.next();
            IPostProcessor obj = Util.getInstance(info.getClassName(), new File(info.getJarFileName()), null, null);
            System.out.println("Loaded Post-Processor: " + info.getClassName() + " in " + info.getJarFileName());
            m_postProcessors.add(obj);
         }
      }
   }
   
   /**
    * From the list of post-processors call all the ones that are registered to handle the target
    * messages, based on the message group name and message model name.
    *  
    * @param transferInfo The transfer info which is just about to being processed by the runtime. 
    */
   public void postProcess( MdmiTransferInfo transferInfo ) {
      if( transferInfo == null )
         throw new IllegalArgumentException("transferInfo is null");
      for( int i = 0; i < m_postProcessors.size(); i++ ) {
         IPostProcessor p = m_postProcessors.get(i);
         if( p.canProcess(transferInfo.targetModel.getGroupName(), transferInfo.targetModel.getModelName()) ) {
            Mdmi.INSTANCE.logger().info("Post-processor {0} called for target model {1}", p.getName(),
                  transferInfo.targetModel.getQualifiedName());
            try {
               p.processMessage(transferInfo.targetMessage, transferInfo.targetModel);
            }
            catch( Exception ex ) {
               throw new MdmiException(
                     ex,
                     "MdmiPostProcessor {0} throws an unexpected exception while processing target of transfer request {1}",
                     p.getName(), transferInfo.toString());
            }
         }
      }
   }
} // MdmiPostProcessors
