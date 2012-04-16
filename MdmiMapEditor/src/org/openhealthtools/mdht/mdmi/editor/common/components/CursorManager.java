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
*     Sally Conway
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.RootPaneContainer;

/**
 * @author SConway
 *
 * Manage the WatchCursor accross different windows
 */
public class CursorManager {
    private static HashMap<RootPaneContainer, CursorManager> m_instances = new HashMap<RootPaneContainer, CursorManager> ();
    private static MouseListener m_glassPaneMouseListener = new MouseEventListener();
    private static KeyListener m_glassPaneKeyListener = new KeyEventConsumer();
    
    private static CursorManager m_emptyInstance = new CursorManager(null);
    
    private RootPaneContainer m_root = null;
    private CursorManager m_parent = null;
    
    // number of instances that are showing the wait cursor
    private int m_waitingCount = 0;
    
    
    /** get the cursor manager for this component */
    public static CursorManager getInstance(Component c) {
        RootPaneContainer rootPaneContainer = getRootPaneContainer(c);
        if (rootPaneContainer == null) {
            return m_emptyInstance;
        }
        // do we have one already?
        CursorManager instance = m_instances.get(rootPaneContainer);
        if (instance == null) {
            instance = new CursorManager(rootPaneContainer);
            m_instances.put(rootPaneContainer, instance);
        }
        return instance;
    }
    
    private static RootPaneContainer getRootPaneContainer(Component c) {
        RootPaneContainer rootPaneContainer = null;
        while (c != null) {
            if (c instanceof RootPaneContainer) {
                rootPaneContainer = (RootPaneContainer)c;
                return rootPaneContainer;
            }
            c = c.getParent();
        }
        return null;
    }
    
    /** There should be one of these per root pane*/
    private CursorManager(RootPaneContainer top) {
        m_root = top;
        // if top has a parent, we want to set it too
        if (top != null && top instanceof Component
                && ((Component)top).getParent() != null) {
            m_parent = getInstance(((Component)top).getParent());
        }
    }
    
    /** Set a "Wait" cursor */
    public synchronized void setWaitCursor() {
        if (m_root == null) {
            return;
        }
        m_waitingCount++;
        
        Component gp = m_root.getRootPane().getGlassPane();
        // add listener to grab mouse and key events
        if (m_waitingCount == 1) {
            gp.addKeyListener(m_glassPaneKeyListener);
            gp.addMouseListener(m_glassPaneMouseListener);
//       System.out.println("Setting WAIT CURSOR");
            gp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            gp.setVisible(true);
            gp.requestFocus();
        }
        if (m_parent != null) {
            m_parent.setWaitCursor();
        }
    }


    /** Restore the cursor to the default */
    public synchronized void restoreCursor() {
        if (m_root == null) {
            return;
        }
        m_waitingCount--;
        if (m_waitingCount == 0) {
            m_instances.remove(m_root);
            Component gp = m_root.getRootPane().getGlassPane();
            
            gp.removeKeyListener(m_glassPaneKeyListener);
            gp.removeMouseListener(m_glassPaneMouseListener);

//            System.out.println("   Setting DEFAULT CURSOR");
            gp.setCursor(Cursor.getDefaultCursor());
            gp.setVisible(false);
            
        } else if (m_waitingCount < 0) {
            m_waitingCount = 0;
        }
        if (m_parent != null) {
            m_parent.restoreCursor();
        }
    }
    
    /** Demo how the cursor manager works with a modal dialog */
    public static void main(String [] args) {
        final JFrame myFrame = new JFrame("Cursor Test");
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        final CursorManager cm = CursorManager.getInstance(myFrame);
        final JLabel label = new JLabel("     Watch here     ");
        JButton startButton = new JButton("Start");
        myFrame.getContentPane().setLayout(new FlowLayout());
        myFrame.getContentPane().add(label);
        myFrame.getContentPane().add(startButton);
        
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                cm.setWaitCursor();
                Thread t = new Thread(new Runnable() {
                    public void run()
                    {
                        boolean keepGoing = true;
                        Color [] colors = {Color.darkGray, Color.gray, Color.white, Color.pink,
                                           Color.magenta, Color.red, Color.orange, Color.yellow, 
                                           Color.green, Color.cyan, Color.blue, Color.black};
                        while (keepGoing) {
                            try {
                                for (int i=0; i<colors.length; i++) {
                                    label.setForeground(colors[i]);
                                    label.revalidate();
                                    Thread.sleep(500);                         
                                }
                                if (JOptionPane.showConfirmDialog(myFrame, "Do you want to continue?",
                                        "Continue", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                    keepGoing = true;
                                } else {
                                    keepGoing = false;
                                }
                                
                            } catch (InterruptedException ex) {
                                
                            }
                        }
                       
                        cm.restoreCursor();    
                    }
                });
                t.start();
                           
            }
            
        });
        
        myFrame.setSize(300,200);
        myFrame.setVisible(true);
        
    }
}

/** Key Listener that consumes all key events */
final class KeyEventConsumer extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        e.consume();
    }
}

/** Mouse Listener that consumes all events */
final class MouseEventListener extends MouseAdapter {
}
