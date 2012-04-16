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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openhealthtools.mdht.mdmi.editor.common.SystemContext;

/**
 * This is the exception details dialog.
 */

public class ExceptionDetailsDialog extends BaseDialog {
    private static final long serialVersionUID = -3722626309122752905L;
	
	private JTextArea m_errorDisplay = new JTextArea(25, 30);
    private JTextArea m_summaryDisplay = new JTextArea(3, 30);

    /** Resources **/
    private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.components.Local");

    /** Show an exception dialog */
    public static void showException(Component pnt, Exception ex) {
		SystemContext.getLogWriter().loge(ex, "Exception being displayed to user");
       
       Frame frame = SystemContext.getApplicationFrame();
       if (frame == null) {
          frame = new JFrame();
       }
       ExceptionDetailsDialog dlg = new ExceptionDetailsDialog(frame, ex);
       dlg.display(pnt);
    }
    
    /**
      * The default constructor.
      *
      * @param frame The parent frame
      */
    public ExceptionDetailsDialog(Frame pnt, String summary, String text)
    {
        super(pnt, BaseDialog.CANCEL_BUTTON_OPTION);

        buildUI();
        loadData(summary, text);
    }
    
    /**
     * The default constructor.
     *
     * @param frame The parent frame
     */
   public ExceptionDetailsDialog(Frame pnt, Exception exc)
   {
       super(pnt, BaseDialog.CANCEL_BUTTON_OPTION);

       buildUI();
       loadData(exc);
   }

   @Override
   public void setInitialFocus()
   {
       m_summaryDisplay.requestFocus();
       m_summaryDisplay.setCaretPosition(0);
   }

   private void loadData(String summaryText, String errorText)
   {
       m_summaryDisplay.setText(summaryText);
       m_errorDisplay.setText(errorText);
       
       
       // set display to be top of file
       SwingUtilities.invokeLater(new Runnable()
               {
                   public void run()
                   {
                       m_errorDisplay.setCaretPosition(0);
                   }
               });
   } 
   
    private void loadData(Exception exc)
    {
        StringBuffer errorText = new StringBuffer("Message:\n");
        StackTraceElement [] trace = exc.getStackTrace();
        for (int i=0; i<trace.length; i++) {
           errorText.append(trace[i]).append("\n");
        }
        
        loadData(exc.toString(), errorText.toString());

    }

    /**
      * Creates the layout for the dialog.
      */
    private void buildUI()
    {
        setTitle(s_res.getString("ExceptionDetailsDialog.title"));

        getContentPane().add(createDisplay(), BorderLayout.CENTER);

        pack();
        setSize(800, 600);
        center();
    }

    /**
      * Creates the panel with the fields.
      *
      * @return JPanel The panel containing the fields
      */
    private JPanel createDisplay()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2,2,2,2);

        JLabel lbl = new JLabel(s_res.getString("ExceptionDetailsDialog.message"));
        panel.add(lbl, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        m_summaryDisplay.setLineWrap(true);
        m_summaryDisplay.setWrapStyleWord(true);
        m_summaryDisplay.setEditable(false);
        m_summaryDisplay.setFont(getFont());
        m_summaryDisplay.setBackground((Color)UIManager.get("control"));


        JScrollPane sp = new JScrollPane();
        sp.getViewport().add(m_summaryDisplay);
        sp.getViewport().setPreferredSize(m_summaryDisplay.getPreferredScrollableViewportSize());
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(sp, gbc);

        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;

        lbl = new JLabel(s_res.getString("ExceptionDetailsDialog.details"));
        panel.add(lbl, gbc);

        gbc.gridy++;
        gbc.weighty = 10;
        gbc.fill = GridBagConstraints.BOTH;

        m_errorDisplay.setLineWrap(true);
        m_errorDisplay.setWrapStyleWord(true);
        m_errorDisplay.setEditable(false);
        m_errorDisplay.setFont(getFont());
        m_errorDisplay.setBackground((Color)UIManager.get("control"));

        sp = new JScrollPane();
        sp.getViewport().add(m_errorDisplay);
        sp.getViewport().setPreferredSize(m_errorDisplay.getPreferredScrollableViewportSize());
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(sp, gbc);

        return panel;
    }


    
    @Override
    public boolean isDataValid() {
       return true;
    }


}