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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Base for OK/Cancel dialogs.
 */
@SuppressWarnings( "serial" )
public class DlgBase extends JDialog {
   protected JPanel    pnlSouth ;
   protected JButton   btnOk    ;
   protected JButton   btnCancel;
   protected boolean   m_ok     ;

   protected static final int MIN_WIDTH  = 150;
   protected static final int MIN_HEIGHT = 100;
   protected static final int PRF_WIDTH  = 240;
   protected static final int PRF_HEIGHT = 120;

   protected static void show( DlgBase dlg ) {
      dlg.setSizeAndPosition();
      dlg.pack();
      dlg.setVisible( true );
   }

   protected DlgBase( Frame owner, String title ) {
      this( owner, title, true );
   }

   protected DlgBase( Dialog owner, String title ) {
      this( owner, title, true );
   }

   protected DlgBase( Frame owner, String title, boolean modal ) {
      super( owner, modal );
      super.setTitle( title );
   }

   protected DlgBase( Dialog owner, String title, boolean modal ) {
      super( owner, modal );
      super.setTitle( title );
   }

   protected Dimension minSize() {
      return new Dimension( MIN_WIDTH, MIN_HEIGHT );
   }

   protected Dimension prefSize() {
      return new Dimension( PRF_WIDTH, PRF_HEIGHT );
   }
   
   protected void onOk() {
      m_ok = true;
      saveUserPreferences();
      this.setVisible( false );
   }

   protected void onCancel() {
      m_ok = false;
      saveUserPreferences();
      this.setVisible( false );
   }
   
   protected void initUI() {
      addComponentListener( new java.awt.event.ComponentAdapter() {
         public void componentResized( ComponentEvent e ) {
            onComponentResized( e );
         }
      } );

      btnOk = new JButton( "OK" );
      btnOk.addActionListener( new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onOk();
         }
      } );

      btnCancel = new JButton( "Cancel" );
      btnCancel.addActionListener( new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onCancel();
         }
      } );

      pnlSouth = new JPanel();
      pnlSouth.setPreferredSize( new Dimension( MIN_WIDTH, 30 ) );

      pnlSouth.add( btnOk );
      pnlSouth.add( btnCancel );

      this.getContentPane().setLayout( new BorderLayout() );
      this.getContentPane().add( pnlSouth, BorderLayout.SOUTH );
   }

   private void onComponentResized( ComponentEvent e ) {
      boolean bResize = false;
      Dimension min = minSize(); 
      int width = this.getWidth();
      if( width < min.width ) {
         width = min.width;
         bResize = true;
      }
      int height = this.getHeight();
      if( height < min.height ) {
         height = min.height;
         bResize = true;
      }
      if( bResize )
         this.setSize( width, height );
   }

   protected void setSizeAndPosition() {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      String prefix = settingsPrefix();
      if( !Main.hasProperty(prefix + "/windowIsMaximized") ) {
         setSize( prefSize() );
         setLocation( (screenSize.width  - getSize().width ) / 2,
                      (screenSize.height - getSize().height) / 2 );
         return;
      }
      int iX = 0, iY = 0, iW = 0, iH = 0;
      if( Main.getPropertyBool(prefix + "/windowIsMaximized") ) {
         iX = 0;
         iY = 0;
         iW = screenSize.width;
         iH = screenSize.height;
      }
      else {
         Dimension min = minSize();
         iX = Main.getPropertyInt( prefix + "/locationX" );
         iY = Main.getPropertyInt( prefix + "/locationY" );
         iW = Main.getPropertyInt( prefix + "/dimensionW" );
         iH = Main.getPropertyInt( prefix + "/dimensionH" );
         if( iX < 0 || screenSize.width  <= iX ) iX = 0;
         if( iY < 0 || screenSize.height <= iY ) iY = 0;
         if( iW < min.width  || screenSize.width  <= iW ) iW = min.width ;
         if( iH < min.height || screenSize.height <= iH ) iH = min.height;
      }
      setLocation( iX, iY );
      setSize( iW, iH );
   }

   protected String settingsPrefix() {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      return "ui/windowSizeAndLocation/screenResolution_"
             + screenSize.width + "." + screenSize.height
             + "/dialogs/" + getClass().getName();
   }
   
   // remember position and size of this dialog
   protected void saveUserPreferences() {
      Point     p = getLocation();
      Dimension d = getSize();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      String prefix = settingsPrefix();
      if( p.x <= 0 && p.y <= 0 && d.width >= screenSize.width && d.height >= screenSize.height - 20 ) {
         Main.setProperty( prefix + "/windowIsMaximized", true );
      }
      else {
      	Main.setProperty( prefix + "/windowIsMaximized", false );
      	Main.setProperty( prefix + "/locationX", p.x );
      	Main.setProperty( prefix + "/locationY", p.y );
      	Main.setProperty( prefix + "/dimensionW", d.width );
      	Main.setProperty( prefix + "/dimensionH", d.height );
      }
      Main.saveProperties();
   }
   
   protected void setEnterEscape( JComponent jc ) {
      Component[] ac = jc.getComponents();
      for( int i = 0; i < ac.length; i++ ) {
         Component c = ac[ i ];
         if( c instanceof JComponent )
            setEnterEscape( (JComponent)c );
      }
      if( jc instanceof JTextComponent || jc instanceof JButton ) {
         jc.getInputMap().put( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterKey" );
         jc.getActionMap().put( "enterKey", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               onOk();
            }
         } );
         jc.getInputMap().put( KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escapeKey" );
         jc.getActionMap().put( "escapeKey", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               onCancel();
            }
         } );
      }
   }
} // DlgBase