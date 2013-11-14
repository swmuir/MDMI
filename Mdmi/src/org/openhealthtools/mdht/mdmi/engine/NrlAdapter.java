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
package org.openhealthtools.mdht.mdmi.engine;

import java.io.*;
import java.util.*;

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.*;

public class NrlAdapter implements IExpressionInterpreter {
   private static boolean        loadedJars = false;
   private static JarClassLoader jl;

   IExpressionInterpreter        m_adapter  = null;

   public NrlAdapter() {
      tryInitAdapter();
   }

   public NrlAdapter( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      tryInitAdapter();
      try {
         initialize(eset, context, name, value);
      }
      catch( Exception ex ) {
      }
   }

   private void tryInitAdapter() {
      if( !loadedJars ) {
         try {
            File i = new File("./NRL/nrl-interpreter.jar");
            File f = new File("./nrl-adapter.jar");
            if( !i.exists() || !f.exists() ) {
               i = new File("./bin/NRL/nrl-interpreter.jar");
               f = new File("./bin/nrl-adapter.jar");
            }
            if( !i.exists() || !f.exists() ) {
               i = new File("../bin/NRL/nrl-interpreter.jar");
               f = new File("../bin/nrl-adapter.jar");
            }
            if( !i.exists() || !f.exists() )
               return;

            jl = new JarClassLoader(i);
            jl.addJarFile(f);
            loadedJars = true;
         }
         catch( Exception ex ) {
            System.out.println(ex.getMessage());
         }
      }

//      try {
//         Class<?> c = jl.findClass("com.whitestar.mdmi.NrlAdapter");
//         Object o;
//         o = c.newInstance();
         m_adapter = new com.whitestar.mdmi.NrlAdapter();
//      }
//      catch( InstantiationException e ) {
//
//         e.printStackTrace();
//      }
//      catch( IllegalAccessException e ) {
//
//         e.printStackTrace();
//      }
   }

   public void initialize( ElementValueSet eset, XElementValue context, String name, XValue value ) {
      if( m_adapter != null )
         m_adapter.initialize(eset, context, name, value);
   }

   public boolean evalConstraint( IElementValue context, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         return m_adapter.evalConstraint(context, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");

      return true;
   }

   public void evalAction( IElementValue context, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         m_adapter.evalAction(context, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");
   }

   public List<String> compileConstraint( SemanticElement se, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         return m_adapter.compileConstraint(se, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");

      return new ArrayList<String>();
   }

   public List<String> compileAction( SemanticElement se, String rule ) {
      if( m_adapter != null && !rule.isEmpty() )
         return m_adapter.compileAction(se, rule);
      else if( !rule.isEmpty() )
         throw new MdmiException("The open source version does not support the use of NRL rules.");

      return new ArrayList<String>();
   }
} // NrlAdapter
