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
package org.openhealthtools.mdht.mdmi.model.raw;

import java.util.*;

public class ModelRoot {
   private Map<String, ClassDef> m_classDefs = new HashMap<String, ClassDef>();

   public void addClassDef( ClassDef def ) {
      m_classDefs.put(def.getId(), def);
   }

   public ClassDef getClass( String id ) {
      return m_classDefs.get(id);
   }

   public Collection<ClassDef> getClassDefs() {
      return m_classDefs.values();
   }
}
