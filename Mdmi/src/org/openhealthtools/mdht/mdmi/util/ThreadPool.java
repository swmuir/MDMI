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
package org.openhealthtools.mdht.mdmi.util;

import java.util.concurrent.*;

/**
 * Fixed pool of threads. Wraps the standard java concurrent classes.
 * 
 * @author goancea
 */
public final class ThreadPool {
   public static final int MIN_THREAD_POOL_SIZE = 1;
   public static final int MAX_THREAD_POOL_SIZE = 100;
   public static final int DEF_THREAD_POOL_SIZE = 5;

   private ExecutorService m_svc;

   /**
    * Start the pool with the given thread pool size. The pool size must be between MIN and MAX inclusive, if not it
    * will be using the default. If already started no action is taken.
    * 
    * @param threadPoolSize no of threads in the pool, must be between MIN and MAX inclusive.
    */
   public void start( int threadPoolSize ) {
      if( m_svc != null )
         return;
      if( threadPoolSize < MIN_THREAD_POOL_SIZE || threadPoolSize > MAX_THREAD_POOL_SIZE )
         threadPoolSize = DEF_THREAD_POOL_SIZE;
      m_svc = Executors.newFixedThreadPool(threadPoolSize);
   }

   /**
    * Shutdown in an orderly fashion, if possible. If it was not started, nothing happens. Will wait 5 seconds, then
    * terminate all.
    */
   public void stop() {
      if( m_svc == null )
         return;
      try {
         m_svc.shutdown();
         m_svc.awaitTermination(5, TimeUnit.SECONDS);
         if( !m_svc.isTerminated() )
            m_svc.shutdownNow();
      }
      catch( Exception ignored ) {
      }
      m_svc = null;
   }

   /**
    * The command passed in will be invoked on the next available thread.
    * 
    * @param command Command to execute (invoke).
    */
   public void submit( Runnable command ) {
      if( m_svc == null )
         throw new IllegalStateException("Thread pool is not started!");
      m_svc.submit(command);
   }

   /**
    * The command passed in will be invoked on the next available thread. When done the returned Future's get() method
    * will return the specified value.
    * 
    * @param <T> Type of the value.
    * @param command Command to execute.
    * @param value Value to return from the get() method of the returned Future.
    * @return Future that is used to determine when the command execution is finished.
    */
   public <T> Future<T> submit( Runnable command, T value ) {
      if( m_svc == null )
         throw new IllegalStateException("Thread pool is not started!");
      return m_svc.submit(command, value);
   }

   /**
    * The task passed in will be invoked on the next available thread. When done the returned Future's get() method will
    * return the value of the Callable task.
    * 
    * @param <T> Type of the callable and future.
    * @param task Callable task (method return a value of type T).
    * @return Future that is used to determine when the command execution is finished and return the value.
    */
   public <T> Future<T> submit( Callable<T> task ) {
      if( m_svc == null )
         throw new IllegalStateException("Thread pool is not started!");
      return m_svc.submit(task);
   }
} // ThreadPool

