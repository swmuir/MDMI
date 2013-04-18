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

import org.openhealthtools.mdht.mdmi.MdmiException;

@SuppressWarnings( "serial" )
public final class DlgShowException extends DlgBase {
   private Exception    m_exception  ;
   private String       m_message    ;
   private JTextPane    txtStackTrace;
   private JTextField   txtMessage   ;

   public static void doModal( Frame owner, Exception ex, String title ) {
      DlgShowException dlg = new DlgShowException( owner, title, ex, null );
      show( dlg );
   }

   public static void doModal( Frame owner, String message, Exception ex, String title ) {
      DlgShowException dlg = new DlgShowException( owner, title, ex, message );
      show( dlg );
   }

   public static void doModal( Dialog owner, Exception ex, String title ) {
      DlgShowException dlg = new DlgShowException( owner, title, ex, null );
      show( dlg );
   }

   public static void doModal( Dialog owner, String message, Exception ex, String title ) {
      DlgShowException dlg = new DlgShowException( owner, title, ex, message );
      show( dlg );
   }

   private DlgShowException( Frame owner, String title, Exception ex, String message ) {
      super( owner, title );
      m_exception = ex;
      m_message   = message;
      initUI();
   }

   private DlgShowException( Dialog owner, String title, Exception ex, String message ) {
      super( owner, title );
      m_exception = ex;
      m_message   = message;
      initUI();
   }

   protected Dimension minSize() {
      return new Dimension( 150, 100 );
   }

   protected Dimension prefSize() {
      return new Dimension( 640, 480 );
   }

   protected void initUI() {
      super.initUI();
      setModal( true );

      txtMessage = new JTextField();
      txtMessage.setEditable( false );
      txtMessage.setText( m_message != null ? m_message : m_exception.getMessage() );
      
      txtStackTrace = new JTextPane();
      txtStackTrace.setEditable( false );
      txtStackTrace.setText(MdmiException.getFullDescription(m_exception));
      JScrollPane scr = new JScrollPane(txtStackTrace);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      scr.setPreferredSize(new Dimension(screenSize.width/2, screenSize.height/2));
      txtStackTrace.select(0, 0);
      
      this.getContentPane().add( txtMessage, BorderLayout.NORTH );
      this.getContentPane().add( scr, BorderLayout.CENTER );
      setEnterEscape( txtMessage );
      setEnterEscape( scr );
      setEnterEscape( pnlSouth );

      btnCancel.setVisible( false );
      
      setSizeAndPosition();
   }
} // DlgShowException
