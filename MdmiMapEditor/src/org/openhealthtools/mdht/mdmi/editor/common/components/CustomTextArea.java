package org.openhealthtools.mdht.mdmi.editor.common.components;

import java.awt.event.KeyEvent;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/** A basic JTextArea that ignore the Tab key as text entry, so it 
 * can be used to move focus to next field */
public class CustomTextArea extends JTextArea {
	
	public CustomTextArea() {
		super();
	}

	public CustomTextArea(Document doc) {
		super(doc);
	}

	public CustomTextArea(int rows, int columns) {
		super(rows, columns);
	}

	/** ignore Tab key as text entry - use it to move focus to next field */
	@Override
	protected void processComponentKeyEvent( KeyEvent e ) {  
		if (e.getID() == KeyEvent.KEY_PRESSED &&  e.getKeyCode() == KeyEvent.VK_TAB ) {
			e.consume();
			if (e.isShiftDown()) {
				transferFocusBackward();
			} else {
				transferFocus();
			}
		}  
		else {  
			super.processComponentKeyEvent( e );  
		}  
	} 
}
