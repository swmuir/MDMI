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
package org.openhealthtools.mdht.mdmi.engine;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.util.*;

/**
 * The entry point for the transfer engine.
 * This class wraps the pool of threads.
 */
public final class MdmiEngine {
   private Mdmi       m_owner;
   private ThreadPool m_threadPool;

   public MdmiEngine( Mdmi owner ) {
      m_owner = owner;
   }

   public void start() {
      if( m_threadPool == null ) {
         m_threadPool = new ThreadPool();
         m_threadPool.start(m_owner.getConfig().getThreadPoolSize());
      }
   }

   public void stop() {
      if( m_threadPool != null ) {
         m_threadPool.stop();
         m_threadPool = null;
      }
   }

   Mdmi getOwner() {
      return m_owner;
   }

   public void executeTransfer( MdmiTransferInfo transferInfo ) {
      MdmiUow uow = new MdmiUow(this, transferInfo);
      uow.run();
   }

   public void executeTransferAsync( MdmiTransferInfo transferInfo ) {
      MdmiUow uow = new MdmiUow( this, transferInfo );
      m_threadPool.submit( uow );
   }
} // MdmiEngine
