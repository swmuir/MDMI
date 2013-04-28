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
package org.openhealthtools.mdht.mdmi.testui;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Log panel displayed at the lower-right. 
 */
@SuppressWarnings( "serial" )
public class PnlLog extends JPanel {
   private JTextPane txtAppLog;

   public PnlLog() {
      initUI();
   }

   public void addText( String text ) {
      try {
         Position p = txtAppLog.getDocument().getEndPosition();
         txtAppLog.getDocument().insertString( p.getOffset() - 1, text + "\n", null );
      } catch( Exception ignored ) {}
   }

   public boolean hasText() {
      return txtAppLog.getText().length() > 0;
   }

   public void clear() {
      try {
         txtAppLog.getDocument().remove( 0, txtAppLog.getDocument().getLength() );
      } catch( Exception ignored ) {}
   }

   public void clearAll() {
      clear();
   }

   private void initUI() {
      txtAppLog = new JTextPane();
      txtAppLog.setPreferredSize( new Dimension(420, 120) );
      txtAppLog.setEditable( false );
      JScrollPane scrLog = new JScrollPane( txtAppLog );
      super.setLayout( new BorderLayout() );
      super.add( scrLog, BorderLayout.CENTER );
   }
} // PnlLog
