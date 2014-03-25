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

import org.openhealthtools.mdht.mdmi.*;
import org.openhealthtools.mdht.mdmi.model.*;
import org.openhealthtools.mdht.mdmi.util.FileUtil;
import org.openhealthtools.mdht.mdmi.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

@SuppressWarnings( { "serial", "rawtypes", "unchecked" } )
public class FrmMain extends JFrame {
   /** Default location for 'normal', i.e. non-maximized, state */
   private static final int         DEF_X = 50;
   private static final int         DEF_Y = 50;

   /** Default dimensions for 'normal', i.e. non-maximized, state */
   private static final int         DEF_W = (int)(Toolkit.getDefaultToolkit().getScreenSize().width * 0.75);
   private static final int         DEF_H = (int)(Toolkit.getDefaultToolkit().getScreenSize().height * 0.75);

   private int                      m_winX;
   private int                      m_winY;
   private int                      m_winW;
   private int                      m_winH;

   private Collection<MessageGroup> sourceMessageGroups;
   private Collection<MessageGroup> targetMessageGroups;

   private JLabel                   lblSource;
   private JLabel                   lblTarget;
   private JLabel                   lblMap;
   private JTextField               txtSourceMap;
   private JButton                  btnSourceMap;
   private JButton                  btnSrcSynPrv;
   private JButton btnUpdateMaps;

   private JTextField               txtTargetMap;
   private JButton                  btnTargetMap;
   private JButton                  btnTrgSynPrv;

   private JLabel                   lblMdl;
   private JComboBox                cmbSourceMdl;

   private JComboBox                cmbTargetMdl;

   private JLabel                   lblMsg;
   private JTextField               txtSourceMsg;
   private JButton                  btnSourceMsg;

   private JTextField               txtTargetMsg;
   private JButton                  btnTargetMsg;
   private JButton                  btnUpdateTargetMsg;

   private JLabel                   lblMessage;
   private JTextArea                txtSourceMessage;
   private JTextArea                txtTargetMessage;
   private JButton                  btnSaveXfrResult;

   private JLabel                   lblElements;
   private JList                    lstElements;
   private JButton                  btnTransfer;

   private PnlLog                   pnlSouth;

   public FrmMain() {
      try {
         initUI();
         loadUserPreferences();

         String last = Main.properties().getProperty("ui.user.lastDirs.lastSourceMapDir");
         if( last != null && last.length() > 0 )
            setSourceMap(last);
         last = Main.properties().getProperty("ui.user.lastDirs.lastTargetMapDir");
         if( last != null && last.length() > 0 )
            setTargetMap(last);
         last = Main.properties().getProperty("ui.user.lastDirs.lastSourceMsgDir");
         if( last != null && last.length() > 0 )
            setSourceMsg(last);
         last = Main.properties().getProperty("ui.user.lastDirs.lastTargetMsgDir");
         if( last != null && last.length() > 0 )
            setTargetMsg(last);
      }
      catch( Exception ex ) {
         JOptionPane.showMessageDialog(null, MdmiException.getFullDescription(ex), "FrmMain fails",
               JOptionPane.ERROR_MESSAGE);
         System.exit(-1);
      }
   }

   void onExit() {
      saveUserPreferences();
      this.dispose();
      System.exit(0);
   }

   void onSelSourceMap() {
      String lastDir = Main.properties().getProperty("ui.user.lastDirs.lastDirectory");
      String lastFile = Main.properties().getProperty("ui.user.lastDirs.lastSourceMapDir");
      if( lastDir == null || lastDir.length() <= 0 )
         lastDir = System.getProperty("user.dir");
      JFileChooser jFileChooser = new JFileChooser(lastDir);
      jFileChooser.setMultiSelectionEnabled(false);
      jFileChooser.setDialogTitle("Select a source map to open");
      jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int r = jFileChooser.showOpenDialog(this);
      if( r != JFileChooser.APPROVE_OPTION )
         return;
      File sf = jFileChooser.getSelectedFile();
      if( sf == null )
         return;
      String lastFileU = sf.getAbsolutePath();
      String lastDirU = sf.getParent();
      File f = new File(lastFileU);
      if( f.exists() ) {
         if( !lastFileU.equals(lastFile) )
            Main.properties().setProperty("ui.user.lastDirs.lastSourceMapDir", lastFileU);
         if( !lastDirU.equals(lastDir) )
            Main.properties().setProperty("ui.user.lastDirs.lastDirectory", lastDirU);
         setSourceMap(lastFileU);
      }
   }
   
   void onSourceSynProv() {
      setSourceMap(txtSourceMap.getText(), "org.openhealthtools.mdht.mdmiplugins.parsers.HL7Parser" , "parsers-0.0.1-SNAPSHOT.jar");
   }
   void onTargetSynProv() {
      setTargetMap(txtTargetMap.getText(), "org.openhealthtools.mdht.mdmiplugins.parsers.HL7Parser" , "parsers-0.0.1-SNAPSHOT.jar");
   }
   
   void onSelTargetMap() {
      String lastDir = Main.properties().getProperty("ui.user.lastDirs.lastDirectory");
      String lastFile = Main.properties().getProperty("ui.user.lastDirs.lastTargetMapDir");
      if( lastDir == null || lastDir.length() <= 0 )
         lastDir = System.getProperty("user.dir");
      JFileChooser jFileChooser = new JFileChooser(lastDir);
      jFileChooser.setMultiSelectionEnabled(false);
      jFileChooser.setDialogTitle("Select a target map to open");
      jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int r = jFileChooser.showOpenDialog(this);
      if( r != JFileChooser.APPROVE_OPTION )
         return;
      File sf = jFileChooser.getSelectedFile();
      if( sf == null )
         return;
      String lastFileU = sf.getAbsolutePath();
      String lastDirU = sf.getParent();
      File f = new File(lastFileU);
      if( f.exists() ) {
         if( !lastFileU.equals(lastFile) )
            Main.properties().setProperty("ui.user.lastDirs.lastTargetMapDir", lastFileU);
         if( !lastDirU.equals(lastDir) )
            Main.properties().setProperty("ui.user.lastDirs.lastDirectory", lastDirU);
         setTargetMap(lastFileU);
      }
   }

   void onUpdateMaps() {
      setSourceMap(txtSourceMap.getText());
      setTargetMap(txtTargetMap.getText());
   }

   void onSelSourceMsg() {
      String lastDir = Main.properties().getProperty("ui.user.lastDirs.lastDirectory");
      String lastFile = Main.properties().getProperty("ui.user.lastDirs.lastSourceMsgDir");
      if( lastDir == null || lastDir.length() <= 0 )
         lastDir = System.getProperty("user.dir");
      JFileChooser jFileChooser = new JFileChooser(lastDir);
      jFileChooser.setMultiSelectionEnabled(false);
      jFileChooser.setDialogTitle("Select a source message to open");
      jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int r = jFileChooser.showOpenDialog(this);
      if( r != JFileChooser.APPROVE_OPTION )
         return;
      File sf = jFileChooser.getSelectedFile();
      if( sf == null )
         return;
      String lastFileU = sf.getAbsolutePath();
      String lastDirU = sf.getParent();
      File f = new File(lastFileU);
      if( f.exists() ) {
         if( !lastFileU.equals(lastFile) )
            Main.properties().setProperty("ui.user.lastDirs.lastSourceMsgDir", lastFileU);
         if( !lastDirU.equals(lastDir) )
            Main.properties().setProperty("ui.user.lastDirs.lastDirectory", lastDirU);
         setSourceMsg(lastFileU);
      }
   }

    void onUpdateTargetMsg(){
        setTargetMsg(txtTargetMsg.getText());
    }

   void onSelTargetMsg() {
      String lastDir = Main.properties().getProperty("ui.user.lastDirs.lastDirectory");
      String lastFile = Main.properties().getProperty("ui.user.lastDirs.lastTargetMsgDir");
      if( lastDir == null || lastDir.length() <= 0 )
         lastDir = System.getProperty("user.dir");
      JFileChooser jFileChooser = new JFileChooser(lastDir);
      jFileChooser.setMultiSelectionEnabled(false);
      jFileChooser.setDialogTitle("Select a target message to open");
      jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int r = jFileChooser.showOpenDialog(this);
      if( r != JFileChooser.APPROVE_OPTION )
         return;
      File sf = jFileChooser.getSelectedFile();
      if( sf == null )
         return;
      String lastFileU = sf.getAbsolutePath();
      String lastDirU = sf.getParent();
      File f = new File(lastFileU);
      if( f.exists() ) {
         if( !lastFileU.equals(lastFile) )
            Main.properties().setProperty("ui.user.lastDirs.lastTargetMsgDir", lastFileU);
         if( !lastDirU.equals(lastDir) )
            Main.properties().setProperty("ui.user.lastDirs.lastDirectory", lastDirU);
         setTargetMsg(lastFileU);
      }
   }

   void onSaveTransformationResult() {
      String lastDir = Main.properties().getProperty("ui.user.lastDirs.lastDirectory");
      String lastSaveDir = Main.properties().getProperty("ui.user.lastDirs.lastSaveTransformationResult");
      if( lastSaveDir == null || lastSaveDir.length() <= 0 ) {
         if( !(lastDir == null || lastDir.length() <= 0) ) {
            lastSaveDir = lastDir;
         }
      }
      JFileChooser jFileChooser = new JFileChooser(lastSaveDir);
      jFileChooser.setDialogTitle("Save transformation result");
      if( jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION ) {
         return;
      }
      File saveFile = null;
      String saveFileName = null;
      try {
         saveFile = jFileChooser.getSelectedFile();
         saveFileName = saveFile.getName();
         if( getExtension(saveFileName) == null ) {
            saveFile = new File(saveFile.getAbsolutePath() + ".xml");
         }
         if( !saveFile.exists() ) {
            saveFile.createNewFile();
         }
         OutputStream fw = new FileOutputStream(saveFile.getAbsoluteFile());
         OutputStreamWriter writer = new OutputStreamWriter(fw, "UTF-8");
         BufferedWriter bw = new BufferedWriter(writer);
         bw.write(txtTargetMessage.getText());
         bw.close();
      }
      catch( IOException ex ) {
         DlgShowException.doModal(this, ex, String.format("Save %s fails", saveFileName));
      }
      if( saveFile.exists() ) {
         Main.properties().setProperty("ui.user.lastDirs.lastSaveTransformationResult", saveFile.getAbsolutePath());
      }
   }

   String getExtension( String fileName ) {
      int index = fileName.lastIndexOf('.');
      if( index <= 0 )
         return null;
      return fileName.substring(index);
   }

   void setSourceMap( String s ) {
      setSourceMap(s, null, null);
   }
   void setSourceMap( String s, String synCN, String synJF ) {
      txtSourceMap.setText(s);
      cmbSourceMdl.removeAllItems();
      try {
         MdmiConfig.MapInfo me = new MdmiConfig.MapInfo("SOURCE", s);
         MdmiConfig.MapInfo oldMe = Mdmi.INSTANCE.getConfig().getMapInfoByFileName(s);
         if( oldMe != null ) {
            me.synSvcJarName = oldMe.synSvcJarName;
            me.synSvcClassName = oldMe.synSvcClassName;
            me.semSvcJarName = oldMe.semSvcJarName;
            me.semSvcClassName = oldMe.semSvcClassName;
         }
         oldMe = Mdmi.INSTANCE.getConfig().getMapInfo(me.mapName);
         if( oldMe != null )
            Mdmi.INSTANCE.getConfig().removeMapInfo(me.mapName);
         if( synCN != null && synJF != null ) {
            me.synSvcJarName = synJF;
            me.synSvcClassName = synCN;
         }
         sourceMessageGroups = Mdmi.INSTANCE.getResolver().resolveOne(me);
         for( MessageGroup mg : sourceMessageGroups ) {
            Collection<MessageModel> mdls = mg.getModels();
            for( MessageModel mdl : mdls ) {
               cmbSourceMdl.addItem(mg.getName() + "." + mdl.getMessageModelName());
            }
         }
         setElements();
      }
      catch( Exception ex ) {
         sourceMessageGroups = null;
         cmbSourceMdl.removeAllItems();
         DlgShowException.doModal(this, ex, "FrmMain.setSourceMap() fails");
      }
   }

   void setTargetMap( String s ) {
      setTargetMap(s, null, null);
   }
   void setTargetMap( String s, String synCN, String synJF ) {
      txtTargetMap.setText(s);
      cmbTargetMdl.removeAllItems();
      try {
         MdmiConfig.MapInfo me = new MdmiConfig.MapInfo("TARGET", s);
         MdmiConfig.MapInfo oldMe = Mdmi.INSTANCE.getConfig().getMapInfoByFileName(s);
         if( oldMe != null ) {
            me.synSvcJarName = oldMe.synSvcJarName;
            me.synSvcClassName = oldMe.synSvcClassName;
            me.semSvcJarName = oldMe.semSvcJarName;
            me.semSvcClassName = oldMe.semSvcClassName;
         }
         oldMe = Mdmi.INSTANCE.getConfig().getMapInfo(me.mapName);
         if( oldMe != null )
            Mdmi.INSTANCE.getConfig().removeMapInfo(me.mapName);
         if( synCN != null && synJF != null ) {
            me.synSvcJarName = synJF;
            me.synSvcClassName = synCN;
         }
         targetMessageGroups = Mdmi.INSTANCE.getResolver().resolveOne(me);
         for( MessageGroup mg : targetMessageGroups ) {
            Collection<MessageModel> mdls = mg.getModels();
            for( MessageModel mdl : mdls ) {
               cmbTargetMdl.addItem(mg.getName() + "." + mdl.getMessageModelName());
            }
         }
         setElements();
      }
      catch( Exception ex ) {
         targetMessageGroups = null;
         cmbTargetMdl.removeAllItems();
         DlgShowException.doModal(this, ex, "FrmMain.setTargetMap() fails");
      }
   }

   private void setElements() {
      lstElements.setListData(new Vector<Object>());
      if( sourceMessageGroups == null || targetMessageGroups == null )
         return;
      ArrayList<MdmiBusinessElementReference> sourceBers = new ArrayList<MdmiBusinessElementReference>();
      for( MessageGroup mg : sourceMessageGroups ) {
         Collection<MdmiBusinessElementReference> bers = mg.getDomainDictionary().getBusinessElements();
         sourceBers.addAll(bers);
      }
      ArrayList<MdmiBusinessElementReference> targetBers = new ArrayList<MdmiBusinessElementReference>();
      for( MessageGroup mg : targetMessageGroups ) {
         Collection<MdmiBusinessElementReference> bers = mg.getDomainDictionary().getBusinessElements();
         targetBers.addAll(bers);
      }

      List<MdmiBusinessElementReference> mutualBEs = new ArrayList<MdmiBusinessElementReference>();
      for( MdmiBusinessElementReference ber : sourceBers ) {
         if( hasBer(targetBers, ber) ) {
            mutualBEs.add(ber);
         }
      }

      Vector<MdmiBusinessElementReference> v = filterBEs(mutualBEs);
      Collections.sort(v, new Comparator<MdmiBusinessElementReference>() {
         @Override
         public int compare( MdmiBusinessElementReference o1, MdmiBusinessElementReference o2 ) {
            String n1 = o1.getName() == null ? "" : o1.getName();
            String n2 = o2.getName() == null ? "" : o2.getName();
            return n1.compareToIgnoreCase(n2);
         }
      });

      lstElements.setListData(v);
      lstElements.setSelectionInterval(0, v.size()-1);
      
   }

   private boolean hasBer( ArrayList<MdmiBusinessElementReference> bers, MdmiBusinessElementReference ber ) {
      for( MdmiBusinessElementReference b : bers ) {
         if( b.getUniqueIdentifier().equals(ber.getUniqueIdentifier()) )
            return true;
      }
      return false;
   }

   private Vector<MdmiBusinessElementReference> filterBEs( List<MdmiBusinessElementReference> businessElements ) {
      Vector<MdmiBusinessElementReference> v = new Vector<MdmiBusinessElementReference>();
      if( cmbSourceMdl.getSelectedItem() != null && cmbTargetMdl.getSelectedItem() != null ) {
         MdmiModelRef sMod = new MdmiModelRef((String)cmbSourceMdl.getSelectedItem());
         MdmiModelRef tMod = new MdmiModelRef((String)cmbTargetMdl.getSelectedItem());

         if( sMod.getModel() != null && tMod.getModel() != null ) {
            for( MdmiBusinessElementReference businessElement : businessElements ) {
               if( hasToBer(sMod.getModel().getElementSet().getSemanticElements(), businessElement)
                     && hasFromBer(tMod.getModel().getElementSet().getSemanticElements(), businessElement) ) {
                  v.add(businessElement);
               }
            }
         }
      }
      return v;
   }

   private boolean hasToBer( Collection<SemanticElement> semanticElements, MdmiBusinessElementReference ber ) {
      if( semanticElements != null && ber != null ) {
         for( SemanticElement semanticElement : semanticElements ) {
            for( ToBusinessElement toBE : semanticElement.getFromMdmi() ) {
               if( toBE.getBusinessElement() != null
                     && toBE.getBusinessElement().getUniqueIdentifier().equals(ber.getUniqueIdentifier()) ) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   private boolean hasFromBer( Collection<SemanticElement> semanticElements, MdmiBusinessElementReference ber ) {
      if( semanticElements != null && ber != null ) {
         for( SemanticElement semanticElement : semanticElements ) {
            for( ToMessageElement toME : semanticElement.getToMdmi() ) {
               if( toME.getBusinessElement() != null
                     && toME.getBusinessElement().getUniqueIdentifier().equals(ber.getUniqueIdentifier()) ) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   void setSourceMsg( String s ) {
      txtSourceMsg.setText(s);
      txtSourceMessage.setText("");
      try {
         File f = new File(s);
         if( !f.exists() )
            throw new RuntimeException("Invalid message file " + f);
         txtSourceMessage.setText(FileUtil.readFile(f));
      }
      catch( Exception ex ) {
         DlgShowException.doModal(this, ex, "FrmMain.setSourceMsg() fails");
      }
   }

   void setTargetMsg( String s ) {
      txtTargetMsg.setText(s);
      txtTargetMessage.setText("");
      try {
         File f = new File(s);
         if( !f.exists() )
            throw new RuntimeException("Invalid message file " + f);
         txtTargetMessage.setText(FileUtil.readFile(f));
      }
      catch( Exception ex ) {
         DlgShowException.doModal(this, ex, "FrmMain.setTargetMsg() fails");
      }
   }

   void onExecuteTransfer() {
      try {
         if( cmbSourceMdl.getSelectedIndex() < 0 )
            throw new RuntimeException("No source message model selected!");
         MdmiModelRef sMod = new MdmiModelRef((String)cmbSourceMdl.getSelectedItem());

         if( txtSourceMessage.getText().length() <= 0 )
            throw new RuntimeException("No source message selected or entered!");
         MdmiMessage sMsg = new MdmiMessage(txtSourceMessage.getText());

         if( cmbTargetMdl.getSelectedIndex() < 0 )
            throw new RuntimeException("No target message model selected!");
         MdmiModelRef tMod = new MdmiModelRef((String)cmbTargetMdl.getSelectedItem());

         if( txtTargetMessage.getText().length() <= 0 )
            throw new RuntimeException("No target message selected or entered!");
         MdmiMessage tMsg = new MdmiMessage(txtTargetMessage.getText());

         @SuppressWarnings( "deprecation" )
         Object[] items = lstElements.getSelectedValues();
         if( items == null || items.length <= 0 )
            throw new RuntimeException("No elements to transfer selected!");
         ArrayList<String> elements = new ArrayList<String>();
         for( int i = 0; i < items.length; i++ ) {
            MdmiBusinessElementReference ber = (MdmiBusinessElementReference)items[i];
            elements.add(ber.getName());
         }

         // sMod.resolve( Mdmi.INSTANCE );
         // IClassifier ua = sMod.getGroup().getDatatype("USAddress");
         // System.out.println( "Original name: " + ua.getOriginalName() );
         // System.out.println( "Qualified name: " + ua.getQualifiedName() );
         // System.out.println( "Attributes: " + ua.getAttributes(true).toString() );

         pnlSouth.addText("Starting transfer...");
         long start = System.currentTimeMillis();
         MdmiTransferInfo ti = new MdmiTransferInfo(sMod, sMsg, tMod, tMsg, elements);
         ti.useDictionary = true;
         Mdmi.INSTANCE.executeTransfer(ti);
         txtTargetMessage.setText(StringUtil.getString(tMsg.getData()));
         long finish = System.currentTimeMillis();
         pnlSouth.addText(String
               .format("Transfer completed successfully. It takes %.3f sec.", (finish - start) / 1000f));
      }
      catch( Exception ex ) {
        DlgShowException.doModal(this, ex, "FrmMain.setSourceMsg() fails");
      }
   }

   private void initUI() {
      lblSource = new JLabel("SOURCE");
      lblTarget = new JLabel("TARGET");
      lblMap = new JLabel("Map:");

      txtSourceMap = new JTextField();
      txtSourceMap.setColumns(48);
      txtSourceMap.setEditable(false);

      btnSourceMap = new JButton("...");
      btnSourceMap.setToolTipText("Select MDMI source map to open.");
      btnSourceMap.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onSelSourceMap();
         }
      });

      btnSrcSynPrv = new JButton("HL7");
      btnSrcSynPrv.setToolTipText("Set source map as HL7.");
      btnSrcSynPrv.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onSourceSynProv();
         }
      });

      btnUpdateMaps = new JButton("Update Maps");
      btnUpdateMaps.setToolTipText("Update source and target maps to open.");
      btnUpdateMaps.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
              onUpdateMaps();
          }
      });

      txtTargetMap = new JTextField();
      txtTargetMap.setColumns(48);
      txtTargetMap.setEditable(false);

      btnTargetMap = new JButton("...");
      btnTargetMap.setToolTipText("Select MDMI target map to open.");
      btnTargetMap.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onSelTargetMap();
         }
      });
      
      btnTrgSynPrv = new JButton("HL7");
      btnTrgSynPrv.setToolTipText("Set source map as HL7.");
      btnTrgSynPrv.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onTargetSynProv();
         }
      });

      lblMdl = new JLabel("Model:");

      cmbSourceMdl = new JComboBox();

      cmbTargetMdl = new JComboBox();

      lblMsg = new JLabel("Message:");

      txtSourceMsg = new JTextField();
      txtSourceMsg.setColumns(48);

      btnSourceMsg = new JButton("...");
      btnSourceMsg.setToolTipText("Select source message file to open.");
      btnSourceMsg.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onSelSourceMsg();
         }
      });

      txtTargetMsg = new JTextField();
      txtTargetMsg.setColumns(48);

      btnTargetMsg = new JButton("...");
      btnTargetMsg.setToolTipText("Select target message file to open.");
      btnTargetMsg.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onSelTargetMsg();
         }
      });

      btnUpdateTargetMsg = new JButton("Update");
      btnUpdateTargetMsg.setToolTipText("Update target message.");
      btnUpdateTargetMsg.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed( ActionEvent e ) {
               onUpdateTargetMsg();
           }
       });

      lblMessage = new JLabel("Message body:");
      txtSourceMessage = new JTextArea();
      txtSourceMessage.setColumns(48);
      txtSourceMessage.setRows(30);
      txtSourceMessage.setFont(new Font("monospaced", 0, 11));
      JScrollPane scrSourceMessage = new JScrollPane(txtSourceMessage);

      txtTargetMessage = new JTextArea();
      txtTargetMessage.setColumns(48);
      txtTargetMessage.setRows(30);
      txtTargetMessage.setFont(new Font("monospaced", 0, 11));
      JScrollPane scrTargetMessage = new JScrollPane(txtTargetMessage);

      btnSaveXfrResult = new JButton("Save");
      btnSaveXfrResult.setToolTipText("Save transformation result");
      btnSaveXfrResult.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed( ActionEvent e ) {
            onSaveTransformationResult();
         }
      });

      lstElements = new JList();
      lstElements.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      lstElements.setCellRenderer(new ElementsRenderer());
    
      JScrollPane scrElements = new JScrollPane(lstElements);
      
        lblElements = new JLabel("Elements to transfer");

      btnTransfer = new JButton("Execute transfer");
      btnTransfer.setToolTipText("Execute the transfer now.");
      btnTransfer.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed( ActionEvent e ) {
            onExecuteTransfer();
         }
      });

      pnlSouth = new PnlLog();

      JPanel p1 = new JPanel();
      p1.add(btnSourceMap);
      p1.add(btnSrcSynPrv);

      JPanel p2 = new JPanel();
      p2.add(btnTargetMap);
      p2.add(btnTrgSynPrv);

       JPanel p3 = new JPanel();
       p3.add(btnTargetMsg);
       p3.add(btnUpdateTargetMsg);

      JPanel pnlCenter = new JPanel(new GridBagLayout());
      pnlCenter.add(lblSource         , new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(lblTarget         , new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(lblMap            , new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(txtSourceMap      , new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(p1                , new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(txtTargetMap      , new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(p2                , new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(lblMdl            , new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(cmbSourceMdl      , new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(btnUpdateMaps     , new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(cmbTargetMdl      , new GridBagConstraints(4, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(lblMsg            , new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(txtSourceMsg      , new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(btnSourceMsg      , new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(txtTargetMsg      , new GridBagConstraints(4, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(p3                , new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(lblMessage        , new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(scrSourceMessage  , new GridBagConstraints(1, 4, 1, 4, 1.0, 1.0, GridBagConstraints.WEST  , GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(btnTransfer       , new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(scrTargetMessage  , new GridBagConstraints(4, 4, 1, 4, 1.0, 1.0, GridBagConstraints.WEST  , GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(btnSaveXfrResult  , new GridBagConstraints(5, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(lblElements       , new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST  , GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      pnlCenter.add(scrElements       , new GridBagConstraints(2, 6, 1, 2, 0.2, 0.2, GridBagConstraints.WEST  , GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

      this.addWindowListener(new WindowAdapter() {
         public void windowClosing( WindowEvent e ) {
            onExit();
         }
      });
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      addWindowStateListener(new MainFrameWindowStateListener());

      this.setTitle("MDMI Runtime Test Tool");
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(pnlCenter, BorderLayout.CENTER);
      this.getContentPane().add(pnlSouth, BorderLayout.SOUTH);
   }

   private void saveUserPreferences() {
      Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
      String node = "ui.windowSizeAndLocation.screenResolution_" + size.width + "x" + size.height + ".";
      Point l = getLocation();
      Dimension d = getSize();
      Properties p = Main.properties();

      if( (super.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH ) {
         p.setProperty(node + "windowIsMaximized", "true");
         p.setProperty(node + "locationX", String.valueOf(m_winX >= 0 ? m_winX : DEF_X));
         p.setProperty(node + "locationY", String.valueOf(m_winY >= 0 ? m_winY : DEF_Y));
         p.setProperty(node + "dimensionW", String.valueOf(m_winW >= 0 ? m_winW : DEF_W));
         p.setProperty(node + "dimensionH", String.valueOf(m_winH >= 0 ? m_winH : DEF_H));
      }
      else {
         p.setProperty(node + "windowIsMaximized", "false");
         p.setProperty(node + "locationX", String.valueOf(l.x));
         p.setProperty(node + "locationY", String.valueOf(l.y));
         p.setProperty(node + "dimensionW", String.valueOf(d.width));
         p.setProperty(node + "dimensionH", String.valueOf(d.height));
      }
      Main.saveProperties();
   }

   private void loadUserPreferences() {
      Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
      String node = "ui.windowSizeAndLocation.screenResolution_" + size.width + "x" + size.height + ".";
      Properties p = Main.properties();
      String x = p.getProperty(node + "locationX");
      String y = p.getProperty(node + "locationY");
      String w = p.getProperty(node + "dimensionW");
      String h = p.getProperty(node + "dimensionH");
      String m = p.getProperty(node + "windowIsMaximized");

      m_winX = x == null ? DEF_X : Integer.valueOf(x);
      m_winY = y == null ? DEF_Y : Integer.valueOf(y);
      m_winW = w == null ? DEF_W : Integer.valueOf(w);
      m_winH = h == null ? DEF_H : Integer.valueOf(h);

      boolean isMax = m == null ? false : "true".equalsIgnoreCase(m);
      if( isMax )
         setExtendedState(JFrame.MAXIMIZED_BOTH);
      else
         setNormalState();
   }

   private void setNormalState() {
      if( m_winW <= 0 ) {
         m_winX = DEF_X;
         m_winY = DEF_Y;
         m_winW = DEF_W;
         m_winH = DEF_H;
      }
      setLocation(m_winX, m_winY);
      setSize(m_winW, m_winH);
   }

   private class MainFrameWindowStateListener implements WindowStateListener {
      public void windowStateChanged( WindowEvent e ) {
         if( e.getNewState() == Frame.NORMAL )
            setNormalState();
      }
   }

   private static class ElementsRenderer extends JLabel implements ListCellRenderer {
      public ElementsRenderer() {
         setOpaque(true);
      }

      public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus ) {
         MdmiBusinessElementReference ber = (MdmiBusinessElementReference)value;
         setText(ber.getName());

         Color background;
         Color foreground;

         // check if this cell represents the current DnD drop location
         JList.DropLocation dropLocation = list.getDropLocation();
         if( dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index ) {

            background = Color.BLUE;
            foreground = Color.WHITE;

            // check if this cell is selected
         }
         else if( isSelected ) {
            background = Color.RED;
            foreground = Color.WHITE;

            // unselected, and not the DnD drop location
         }
         else {
            background = Color.WHITE;
            foreground = Color.BLACK;
         }
         ;

         setBackground(background);
         setForeground(foreground);

         return this;
      }
   }
} // FrmMain
