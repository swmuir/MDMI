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
/*
 * Created on Nov 2, 2005
 *
 */
package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * @author Conway
 *
 * Base class for dialogs. The dialog will be created using a BorderLayout,
 * with the CENTER reserved for the derived classes, and the SOUTH configured
 * as a button panel.
 * 
 * Standard buttons are added by specifying the appropriate button option in the
 * constructor (ex. OK_CANCEL_BUTTON or OK_BUTTON_OPTION + CANCEL_BUTTON_OPTION). Button actions are
 * handled by the eponymously named (big word!) action handler - ex. OkButtonAction,
 * CancelButtonAction, etc. The Enter key will result in a call to the enterKeyAction, which
 * by default will simulate the OK button (or YES button). Likewise the
 * ESC key will call the escapeKeyAction, which will simulate clicking the CANCEL/CLOSE/NO button.
 * 
 * Derived classes should take advantage of the <i>dirty</i> indicator. In general, the
 * OK button will not be enabled until changes have been made (and these changes are valid).
 * The pattern to use is to add appropriate listeners to the input components, and invoke
 * <i>setDirty(true)</i> when data is changed. The base component will enable the OK
 * button after calling <i>isDataValid()</i>.
 * 
 * <b>Critical</b> - All derived classes MUST overload the <i>dispose()</i> method, and use it
 * to remove any listeners that have been added to components (such as ActionListeners,
 * DocumentListeners, etc.). Failure to do so will prevent the dialog from being
 * garbage collected, and will lead to memory issues.
 */
public abstract class BaseDialog extends JDialog {

	/**
	 * generated ID
	 */
	private static final long serialVersionUID = 7290778393474687374L;

	private static final int OFFSET = 30;

	/** Resource for localization */
	private static ResourceBundle s_res = ResourceBundle.getBundle("org.openhealthtools.mdht.mdmi.editor.common.components.Local");

	// Location/Context of last dialog
	private static GraphicsConfiguration s_graphicsContext = null;
	private static Point s_windowLocation = new Point(-OFFSET,-OFFSET);
	
	
	private static final KeyStroke ENTER_KEY  = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	private static final String ENTER  = "enter";
	private static final KeyStroke ESCAPE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private static final String ESCAPE = "escape";


	// BUTTON OPTIONS
	/** OK Button */
	public static final int OK_BUTTON_OPTION     = 1;
	/** Cancel Button */
	public static final int CANCEL_BUTTON_OPTION = 2;
	/** Apply Button */
	public static final int APPLY_BUTTON_OPTION  = 4;
	/** Close Button */
	public static final int CLOSE_BUTTON_OPTION  = 8;
	/** Yes Button */
	public static final int YES_BUTTON_OPTION    = 16;
	/** No Button */
	public static final int NO_BUTTON_OPTION     = 32;

	/** OK and Cancel */
	public static final int OK_CANCEL_OPTION    = OK_BUTTON_OPTION + CANCEL_BUTTON_OPTION;
	/** OK, Cancel and Apply */
	public static final int OK_CANCEL_APPLY_OPTION    = OK_BUTTON_OPTION + CANCEL_BUTTON_OPTION + APPLY_BUTTON_OPTION;
	/** OK and Close */
	public static final int OK_CLOSE_OPTION     = OK_BUTTON_OPTION + CLOSE_BUTTON_OPTION;
	/** Yes and No */
	public static final int YES_NO_OPTION       = YES_BUTTON_OPTION + NO_BUTTON_OPTION;

	// BUTTONS
	protected JButton m_okButton = new JButton(s_res.getString("BaseDialog.okButtonLabel"));
	protected JButton m_cancelButton = new JButton(s_res.getString("BaseDialog.cancelButtonLabel"));
	protected JButton m_applyButton = new JButton(s_res.getString("BaseDialog.applyButtonLabel"));
	protected JButton m_closeButton = new JButton(s_res.getString("BaseDialog.closeButtonLabel"));
	protected JButton m_yesButton = new JButton(s_res.getString("BaseDialog.yesButtonLabel"));
	protected JButton m_noButton = new JButton(s_res.getString("BaseDialog.noButtonLabel"));

	protected JPanel m_buttonPane  = new JPanel(new FlowLayout(FlowLayout.RIGHT));

	private boolean m_ignoreEnterEscKey = false;

	// Data
	private boolean m_good    = true; // good so far
	private int m_buttonOptions = 0;    // selected options
	private int m_exitStatus = 0;       // button selected when closing
	private boolean m_isDirty = false;  // flag to indicate that data has been modified
	private boolean m_isReadOnly = false;

	// BUTTON ACTIONS
	protected ActionListener m_buttonActionListener = new ButtonPressListener();


	/** Create a modal dialog with the default buttons (OK and Cancel) */
	public BaseDialog(Frame owner) {
		super(owner, true);

		initDialog(OK_CANCEL_OPTION);
	}

	/** Create a modal dialog with the specified buttons */
	public BaseDialog(Frame owner, int options) {
		super(owner, true);

		initDialog(options);
	}


	/** Create a modal dialog with the specified buttons */
	public BaseDialog(Dialog owner, int options) {
		super(owner, true);

		initDialog(options);
	}

	/** Create a modal dialog with the default buttons (OK and Cancel) */
	public BaseDialog(Dialog owner) {
		super(owner, true);

		initDialog(OK_BUTTON_OPTION + CANCEL_BUTTON_OPTION);
	}

	/**
	 * initialize the dialog
	 */
	private void initDialog(int options) {
		m_buttonOptions = options;

		buildUI();

		// Add actions for Enter and Escape keys to close dialog
		InputMap inputMap = ((JComponent)getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = ((JComponent)getContentPane()).getActionMap();
		inputMap.put(ENTER_KEY, ENTER);
		actionMap.put(ENTER, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ignoreEnterEscKey()) {
					enterKeyAction();
				}
			}
		});
		
		inputMap.put(ESCAPE_KEY, ESCAPE);
		actionMap.put(ESCAPE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ignoreEnterEscKey()) {
					escapeKeyAction();
				}
			}
		});
		
		addWindowListener();
	}

	/** Split the button area into two (left and right) panels, each with its own
	 * FlowLayout. The Left panel will be left justified, and the right panel will be
	 * right justified, so that buttons will appear visually distinct. Any buttons currently
	 * in the button panel will be placed in the right */
	protected JPanel[] splitButtonArea() {
		JPanel   leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel   rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		// remove existing buttons from buttonPane and put them in the right pane
		for (Component c : m_buttonPane.getComponents()) {
			m_buttonPane.remove(c);
			rightPanel.add(c);
		}

		// re-layout
		m_buttonPane.setLayout(new GridBagLayout());
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.weightx = 1;
		buttonConstraints.weighty = 1;
		buttonConstraints.fill = GridBagConstraints.NONE;

		buttonConstraints.anchor = GridBagConstraints.WEST;
		m_buttonPane.add(leftPanel, buttonConstraints);

		buttonConstraints.anchor = GridBagConstraints.EAST;
		m_buttonPane.add(rightPanel, buttonConstraints);

		return new JPanel[] {leftPanel, rightPanel};

	}

	/** Mark this text field as having changes by coloring it in
	 * the specified color and weight */
	public static void emphasizeText(JComponent field, Color color, int fontStyle) {
		field.setForeground(color);
		field.setFont(field.getFont().deriveFont(fontStyle));
	}

	/** Mark this text field as having changes by coloring it in
	 * the specified color and bold */
	public static void emphasizeText(JComponent field, Color color) {
		emphasizeText(field, color, Font.BOLD);
	}

	/** set the dialog size, but don't allow it to be larger than the screen
	 * dimensions  */
	public void setSizeToFit(Dimension size) {
		// don't exceede display size
		Rectangle displaySize = getUsableScreenBounds(getGraphicsConfiguration());
		Dimension availSize = new Dimension( Math.min(size.width, displaySize.width),
				Math.min(size.height, displaySize.height));

		setSize(availSize);
		validate();
	}

	/**
	 * pack, but enforce minimum size
	 */
	public static void pack(Window window, Dimension minimum) {
		window.pack();

		Dimension size = window.getPreferredSize();
		if (size.width < minimum.width || size.height < minimum.height) {
			size.width = Math.max(size.width, minimum.width);
			size.height = Math.max(size.height, minimum.height);
			window.setSize(size);
		}
	}

	/**
	 * pack, but enforce minimum size
	 */
	public void pack(Dimension minimum) {
		pack(this, minimum);
	}

	/**
	 * center this dialog on the screen
	 */
	public void center()
	{
		centerOnScreen(this);
	}

	/**
	 * center the component on the screen
	 */
	public static void centerOnScreen(Component comp)
	{
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension size = comp.getSize();
		comp.setLocation((screen.width - size.width)/ 2, (screen.height - size.height)/ 2);
	}


	/** Set this window's location relative to the last window, so that
	 * each window is offset to the 30 pixels to theright and below 
	 * the previous one.
	 * @param window
	 */
	public void setLocationWithOffset() {
		setLocationWithOffset(this);
	}

	public void setReadOnly() {
		m_isReadOnly = true;
		Component[] comps = this.getContentPane().getComponents();
		for (int i=0; i<comps.length; i++) {
			if (comps[i] != m_buttonPane)
				setReadOnly( comps[i] );
		}
	}

	public boolean isReadOnly() {
		return m_isReadOnly;
	}

	public void setReadOnly(Component comp) {
		if (comp instanceof JTextComponent)
			((JTextComponent)comp).setEditable( false );
		else if (comp instanceof AbstractButton)
			((AbstractButton)comp).setEnabled( false );
		else if (comp instanceof JComboBox)
			((JComboBox)comp).setEnabled( false );
		else if (comp instanceof JTree)
			((JTree)comp).setEnabled( false );
		else if (comp instanceof JList)
			((JList)comp).setEnabled( false );
		else if (comp instanceof Container) {
			Component comps[] = ((Container)comp).getComponents();
			for (int i=0; i<comps.length; i++) {
				if (comps[i] != m_buttonPane)
					setReadOnly( comps[i] );
			}
		}
	}

	/** Set this window's location relative to the last window, so that
	 * each window is offset 30 pixels to the right and below 
	 * the previous one, but never off screen
	 * @param window
	 */
	public static void setLocationWithOffset(Window window) {
		int incrX = OFFSET;
		int incrY = OFFSET;

		synchronized (s_windowLocation) {
			//       Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			GraphicsConfiguration gc = window.getGraphicsConfiguration();
			if (gc != s_graphicsContext) {
				s_graphicsContext = gc;
				// reset starting point
				s_windowLocation.x = -incrX;
				s_windowLocation.y = -incrY;
			}

			Rectangle screen = getUsableScreenBounds(gc);

			int maxRight = screen.x + screen.width;
			int maxBottom = screen.y + screen.height;

			// check bottom and right edges to be sure they're on the screen

			Dimension windowSize = window.getSize();

			int right = s_windowLocation.x + incrX + windowSize.width;
			int bottom = s_windowLocation.y + incrY + windowSize.height;

			if (right > maxRight) {
				incrX = maxRight - windowSize.width - s_windowLocation.x;
			}
			if (bottom > maxBottom) {
				incrY = maxBottom - windowSize.height - s_windowLocation.y;
			}

			// if both X and Y dimensions are at the edge, then reset
			if (incrX < OFFSET && incrY < OFFSET) {
				s_windowLocation.x = 0;
				s_windowLocation.y = 0;
			} else {
				s_windowLocation.x += incrX;
				s_windowLocation.y += incrY;
			}

			// make sure top and left are on screen
			s_windowLocation.x = Math.max(screen.x, s_windowLocation.x);
			s_windowLocation.y = Math.max(screen.y, s_windowLocation.y);

			window.setLocation(s_windowLocation);
		}
	}

	/** Get the usable screen bounds for this context. 
	 * @param gc    the current GraphicsConfiguration
	 * @return
	 */
	public static Rectangle getUsableScreenBounds(GraphicsConfiguration gc) {
		Rectangle screen = gc.getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		screen.x += insets.left;
		screen.y += insets.top;
		screen.width -= insets.left + insets.right;
		screen.height -= insets.top + insets.bottom;
		return screen;
	}

	/** Set the window location below the bottom edge of the provided component
	 *  (if there is room on the screen).
	 * @param window    The window to be shown
	 * @param c         The component whos location will be used to define the window
	 *                  position
	 */
	public static void showWindowBelowComponent(Window window, Component c) {
		Point location = c.getLocationOnScreen();
		location.y += c.getHeight();

		// adjust if off screen
		location = getAdjustedLocation(window, location);
		window.setLocation(location);
	}

	/** Get the adjusted location of this component so that it will fit
	 *  within the screen bounds. For example, if the right edge of the component
	 *  will extend 50 pixels beyond the usable screen bounds, then the adjusted location
	 *  will be 50 pixels to the left of the original location.
	 *  
	 *  If the component is taller or wider than the usable screen dimensions, it
	 *  will be positioned at the top (or left) of the screen.
	 */
	public static Point getAdjustedLocation(Component component, Point originalLocation) {
		Point location = new Point(originalLocation);
		Rectangle screenBounds = getUsableScreenBounds(component.getGraphicsConfiguration());

		int maxWidth = screenBounds.x + screenBounds.width;
		if ((location.x + component.getWidth()) > maxWidth) {
			// shift left if necessary
			location.x = maxWidth - component.getWidth();
		}
		location.x = Math.max(location.x, screenBounds.x);

		int maxHeight = screenBounds.y + screenBounds.height;
		if ((location.y + component.getHeight()) > maxHeight) {
			// shift up if necessary
			location.y = maxHeight - component.getHeight();
		}
		location.y = Math.max(location.y, screenBounds.y);

		return location;
	}

	/**
	 * Center this dialog in relation to a given component.
	 *
	 * @param comp The outer component
	 */
	public void centerInComponent(Component comp)
	{
		setLocationRelativeTo(comp);
	}

	/** Get the exit status (button ID) when the dialog is closed */
	public int getExitStatus() {
		return m_exitStatus;
	}

	/** Set the status to indicate a problem during construction */
	protected void setFailure() {
		m_good = false;
	}

	/** Display the dialog, centered on the component, and return the exit status when closed */
	public int display(Component comp) {
		centerInComponent(comp);
		return display();
	}

	/** Display the dialog, and return the exit status when closed */
	public int display() {
		if (m_good) {
			setVisible(true);
			toFront();
			return m_exitStatus;
		} else {
			// return immediately
			dispose();
			return CANCEL_BUTTON_OPTION;
		}
	}

	@Override
	public void setVisible(boolean vis) {
		if (vis && !m_good) {
			// kill dialog if no-good
			dispose();
		} else {
			super.setVisible(vis);
		}

	}

	/** Determine whether data has been modified */
	public boolean isDirty() {
		return m_isDirty;
	}

	/** Indicate whether data has been modified. By default, the OK button
	 * will be enabled if dirty */
	public void setDirty(boolean dirty) {
		m_isDirty = dirty;

		// validate
		boolean enable = (dirty && isDataValid());
		m_okButton.setEnabled(enable);
		m_yesButton.setEnabled(enable);
		m_applyButton.setEnabled(enable);
	}

	/** Validate the data. This will be called when setDirty() is called to enable the OK button */
	public abstract boolean isDataValid();


	/** Display a modal message to the user when data is not valid. Typically this is
	 * performed when the OK button is pressed.
	 */
	public void showError(String errorTitle, String errorMessage) {
		showError(this, errorTitle, errorMessage);
	}

	/** Display a modal message to the user when data is not valid. Typically this is
	 * performed when the OK button is pressed.
	 */
	public static void showError(Component comp, String errorTitle, String errorMessage) {
		showErrorDialog(comp, errorTitle, errorMessage, true);
	}
	/** Display a dialog to the user when data is not valid. Typically this is
	 * performed when the OK button is pressed.
	 * @param comp  the parent component for displaying
	 * @param errorTitle    dialog title
	 * @param errorMessage  dialog message
	 * @param modal dialog modality
	 */
	public static JDialog showErrorDialog(Component comp, String errorTitle, String errorMessage, boolean modal) {
		WrappingDisplayText messageArea = new WrappingDisplayText(errorMessage);
		JOptionPane pane = new JOptionPane(messageArea, JOptionPane.WARNING_MESSAGE);
		JDialog dlg = pane.createDialog(comp, errorTitle);
		dlg.setModal(modal);
		dlg.setVisible(true);
		return dlg;
	}


	/** Show an error message when data is missing. The message should be in the form:
	 * <code>Some data is missing.\n{0}</code>.
	 * This method will format the strings in the provided collection, and add these to the
	 * message that is displayed.
	 * @param title     Title to display on error message
	 * @param message   Un-formatted error message.
	 * @param missingFields Collection of fields that are missing
	 * @param focus     Component to set focus on following display of error message
	 * 
	 */
	public void showMissingDataMessage(String title, String message, Collection<String> missingFields, Component focus) {
		showMissingDataMessage(this, title, message, missingFields, focus);
	}

	/** Show an error message when data is missing. The message should be in the form:
	 * <code>Some data is missing.\n{0}</code>.
	 * This method will format the strings in the provided collection, and add these to the
	 * message that is displayed.
	 * @param comp      parent component for error message
	 * @param title     Title to display on error message
	 * @param message   Un-formatted error message.
	 * @param missingFields Collection of fields that are missing
	 * @param focus     Component to set focus on following display of error message
	 * 
	 */
	public static void showMissingDataMessage(Component comp, String title, String message, 
			Collection<String> missingFields, Component focus) {

		StringBuffer msg = new StringBuffer();
		for (String fieldName : missingFields) {
			if (msg.length() > 0) {
				msg.append("\n");
			}
			msg.append("         ").append(fieldName);
		}

		// set focus on missing field
		if (focus != null) {
			requestFocus(focus);
		}
		showError(comp, title, MessageFormat.format(message, new Object[]{msg.toString()}));
		// set focus on missing field again
		if (focus != null) {
			requestFocus(focus);
		}
	}

	/** Set focus on this component. If the component is a text field,
	 * the text will be selected. If the component is in a tab pane, that tab
	 * will be selected. */
	static public void requestFocus(Component focus) {
		// bring parent container to front first
		Component comp = focus;
		while (comp != null) {
			Container parent = comp.getParent();
			if (parent instanceof JTabbedPane) {
				((JTabbedPane)parent).setSelectedComponent(comp);
			} else if (parent instanceof Window) {
				((Window)parent).toFront();
			}
			comp = parent;
		}

		focus.requestFocus();

		if (focus instanceof JTextComponent) {
			JTextComponent textField = (JTextComponent)focus;
			textField.setSelectionStart(0);
			textField.setSelectionEnd(textField.getText().length());
		}
	}

	/**
	 * Utility method that derived classes implement to set focus initially.
	 * This is needed because window must be opened before you can set focus.
	 * This will be called once a window is made visible.
	 */
	public void setInitialFocus() {
	}

	/** add default handler for window events.
	 * On open, calls setInitialFocus(),
	 * on close calls
	 * cancelButtonAction() to dispose the window
	 */
	protected void addWindowListener()
	{
		WindowListener l = new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				cancelButtonAction();
			}
			/**
			 * Grab the window opened event so we can set initial focus.
			 *
			 * @param event WindowEvent
			 */
			@Override
			public void windowOpened(WindowEvent event)
			{
				if(event.getID() == WindowEvent.WINDOW_OPENED)
				{
					setInitialFocus();
				}
			}
			/**
			 * Grab the window opened event so we can set initial focus.
			 *
			 * @param event WindowEvent
			 */
			@Override
			public void windowClosed(WindowEvent event)
			{
				removeWindowListener(this);
			}
		};

		addWindowListener(l);
	}

	/** Remove all listeners when dialog is finished.
	 * This is critical to ensure that memory is released. */
	@Override
	public void dispose() {

		// Remove listeners
		if (isOptionSet(OK_BUTTON_OPTION)) {
			m_okButton.removeActionListener(m_buttonActionListener);
		}
		if (isOptionSet(CANCEL_BUTTON_OPTION)) {
			m_cancelButton.removeActionListener(m_buttonActionListener);
		}
		if (isOptionSet(APPLY_BUTTON_OPTION)) {
			m_applyButton.removeActionListener(m_buttonActionListener);
		}
		if (isOptionSet(CLOSE_BUTTON_OPTION)) {
			m_closeButton.removeActionListener(m_buttonActionListener);
		}
		if (isOptionSet(YES_BUTTON_OPTION)) {
			m_yesButton.removeActionListener(m_buttonActionListener);
		}
		if (isOptionSet(NO_BUTTON_OPTION)) {
			m_noButton.removeActionListener(m_buttonActionListener);
		}
		
		// Remove key bindings
		InputMap inputMap = ((JComponent)getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = ((JComponent)getContentPane()).getActionMap();
		inputMap.remove(ENTER_KEY);
		actionMap.remove(ENTER);
		
		inputMap.remove(ESCAPE_KEY);
		actionMap.remove(ESCAPE);

		// remove all children so garbage collection works better
		try {
			WindowUtil.removeAllComponents(this);
		} catch (Exception ex) {
		}

		super.dispose();
	}
	
	/** Determine whether a button option has been selected */
	public boolean isOptionSet(int option) {
		return (m_buttonOptions & option) != 0;
	}

	/** Create the basic ui */
	private void buildUI() {
		// set layout
		getContentPane().setLayout(new BorderLayout());

		// fill button pane
		// Add buttons and button listeners
		if (isOptionSet(OK_BUTTON_OPTION)) {
			m_buttonPane.add(m_okButton);
			m_okButton.addActionListener(m_buttonActionListener);
			m_okButton.setEnabled(false);   //disable initially
		}
		if (isOptionSet(CANCEL_BUTTON_OPTION)) {
			m_buttonPane.add(m_cancelButton);
			m_cancelButton.addActionListener(m_buttonActionListener);
		}
		if (isOptionSet(APPLY_BUTTON_OPTION)) {
			m_buttonPane.add(m_applyButton);
			m_applyButton.addActionListener(m_buttonActionListener);
		}
		if (isOptionSet(CLOSE_BUTTON_OPTION)) {
			m_buttonPane.add(m_closeButton);
			m_closeButton.addActionListener(m_buttonActionListener);
		}
		if (isOptionSet(YES_BUTTON_OPTION)) {
			m_buttonPane.add(m_yesButton);
			m_yesButton.addActionListener(m_buttonActionListener);
			m_yesButton.setEnabled(false);   //disable initially
		}
		if (isOptionSet(NO_BUTTON_OPTION)) {
			m_buttonPane.add(m_noButton);
			m_noButton.addActionListener(m_buttonActionListener);
		}

		if (m_buttonOptions > 0)
			getContentPane().add(m_buttonPane, BorderLayout.SOUTH);

	}

	/////////////////
	// BUTTON ACTIONS
	/////////////////

	/** Action called when OK button (or the Enter key) is pressed.
	 * Dialog will be closed, and exit status set to OK_BUTTON_OPTION */
	protected void okButtonAction() {
		m_exitStatus = OK_BUTTON_OPTION;

		dispose();
	}

	/** Action called when Apply button is pressed.
	 * Dirty flag will be cleared.
	 * Dialog will remain open */
	protected void applyButtonAction() {
		setDirty(false);
	}


	/** Action called when Cancel button (or the ESC key) is pressed.
	 * Dialog will be closed, and exit status set to CANCEL_BUTTON_OPTION */
	protected void cancelButtonAction() {
		m_exitStatus = CANCEL_BUTTON_OPTION;
		dispose();
	}

	/** Action called when Close button (or the ESC key) is pressed.
	 * Dialog will be closed, and exit status set to CLOSE_BUTTON_OPTION */
	protected void closeButtonAction() {
		m_exitStatus = CLOSE_BUTTON_OPTION;
		dispose();
	}

	/** Action called when Yes button (or the Enter key) is pressed.
	 * Dialog will be closed, and exit status set to YES_BUTTON_OPTION */
	protected void yesButtonAction() {
		m_exitStatus = YES_BUTTON_OPTION;
		dispose();
	}

	/** Action called when No button (or the ESC key) is pressed.
	 * Dialog will be closed, and exit status set to NO_BUTTON_OPTION */
	protected void noButtonAction() {
		m_exitStatus = NO_BUTTON_OPTION;
		dispose();
	}

	/** Ignore Enter or Esc key */
	protected boolean ignoreEnterEscKey() {
		// default is to not-ignore
		return m_ignoreEnterEscKey;
	}

	/** Indicate that the dialog should ignore Enter or Esc key action */
	public void setIgnoreEnterEscKey(boolean ignore) {
		m_ignoreEnterEscKey = ignore;
	}

	/** Action called when the Enter key is pressed. By default this
	 * will simulate the OK or Yes button selection.
	 */
	protected void enterKeyAction() {
		if (isOptionSet(OK_BUTTON_OPTION)) {
			doClick(m_okButton);
		} else if (isOptionSet(YES_BUTTON_OPTION)) {
			doClick(m_yesButton);
		}
	}

	/** Action called when the ESC key is pressed. By default this
	 * will simulate clicking the CANCEL / CLOSE / NO button.
	 */
	protected void escapeKeyAction() {
		if (isOptionSet(CANCEL_BUTTON_OPTION)){
			doClick(m_cancelButton);
		} else if (isOptionSet(CLOSE_BUTTON_OPTION)) {
			doClick(m_closeButton);
		} else if (isOptionSet(NO_BUTTON_OPTION)) {
			doClick(m_noButton);
		}
	}

	/** process a button click in a new thread */
	protected void doClick(final JButton button) {
		button.requestFocusInWindow();

		// process click in new thread so focus listeners
		// can to their thing.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				button.doClick();
			}
		});

	}


	/////////////////
	// INNER CLASSES
	/////////////////

	/** Listener for button presses */
	private class ButtonPressListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			// call appropriate action
			Object source = event.getSource();
			CursorManager cm = CursorManager.getInstance(BaseDialog.this);

			try {
				cm.setWaitCursor();

				if (source == m_okButton) {
					okButtonAction();

				} else if (source == m_cancelButton) {
					cancelButtonAction();

				} else if (source == m_applyButton) {
					applyButtonAction();

				} else if (source == m_closeButton) {
					closeButtonAction();

				} else if (source == m_yesButton) {
					yesButtonAction();

				} else if (source == m_noButton) {
					noButtonAction();
				}

			} catch (Exception e) {
				ExceptionDetailsDialog.showException(BaseDialog.this, e);

			} finally {
				cm.restoreCursor();
			}

		}
	}


	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setVisible(true);
		TestDialog testDialog = new TestDialog(frame);
		testDialog.center();
		int val = testDialog.display();
		System.out.println("Dialog closed with " 
				+ (val==OK_BUTTON_OPTION ? "OKAY" : "CANCEL"));
		
		System.exit(0);
	}

	private static class TestDialog extends BaseDialog {

		public TestDialog(Frame owner) {
			super(owner, BaseDialog.OK_CANCEL_OPTION);
			
			// add a bunch of components
			JPanel main = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx=0;
			gbc.gridy=0;
			getContentPane().add(main, BorderLayout.CENTER);
			
			JPanel p1 = new JPanel(new GridLayout(2, 2));
			p1.add(new JTextField("    --- F1 ---    "));
			p1.add(new JCheckBox("Pick Me"));
			p1.add(new JTextField("    --- F3 ---    "));
			p1.add(new JComboBox(new String[] {"apples", "peaches", "bananas"}));
			main.add(p1, gbc);

			gbc.gridy++;
			JTextArea area = new JTextArea(3, 20);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			main.add(area, gbc);
			
			gbc.gridy++;
			
			JTable simpleTable = new JTable(new String[][]{
					{"row 0, col 0", "row 0, col 1"},
					{"row 1, col 0", "row 1, col 1"},
					{"row 2, col 0", "row 2, col 1"}}, new String[] {"Column 0", "Column 1"});
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1.0;
			main.add(new JScrollPane(simpleTable), gbc);
			
			setDirty(true);
			
			pack();
		}

		@Override
		public boolean isDataValid() {
			return true;
		}
		
	}
}

