package org.openhealthtools.mdht.mdmi.editor.be_editor.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openhealthtools.mdht.mdmi.editor.common.components.WrappingDisplayText;

public class LeaveOrReplaceDialog {
	public enum LeaveReplaceOption {
		Unknown, Leave, LeaveAll, Replace, ReplaceAll, Cancel;
	}
	
	/** Display a dialog prompting the user that there are duplicate names */
	public static LeaveReplaceOption ShowDialog(Component parentComponent, String elementType, String elementName) {
		LeaveReplaceOption status = LeaveReplaceOption.Cancel;
		
		String title = elementType + " Exists";
		
		String text = "A " + elementType + " named '" + elementName + "' already exists.";
		JPanel message = new JPanel(new BorderLayout());
		message.add(new WrappingDisplayText(text), BorderLayout.NORTH);
		
		JRadioButton leaveButton = new JRadioButton("Leave the existing " + elementType, true);
		JRadioButton replaceButton = new JRadioButton("Replace the existing " + elementType + " with this one", false);
		ButtonGroup group = new ButtonGroup();
		group.add(leaveButton);
		group.add(replaceButton);
		JPanel centerPanel = new JPanel(new GridLayout(0, 1));
		centerPanel.add(leaveButton);
		centerPanel.add(replaceButton);
		centerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"What would you like to do?"));
		message.add(centerPanel, BorderLayout.CENTER);
		
		JCheckBox applyToAll = new JCheckBox("Apply to all " + elementType + "s");
		message.add(applyToAll, BorderLayout.SOUTH);

		int rc = JOptionPane.showConfirmDialog(parentComponent, message, title,
				JOptionPane.OK_CANCEL_OPTION );
		
		
		// see what's been checked
		if (rc != JOptionPane.OK_OPTION) {
			status = LeaveReplaceOption.Cancel;
		} else if (leaveButton.isSelected()) {
			status = (applyToAll.isSelected() ? LeaveReplaceOption.LeaveAll : LeaveReplaceOption.Leave);
		} else if (replaceButton.isSelected()) {
			status = (applyToAll.isSelected() ? LeaveReplaceOption.ReplaceAll : LeaveReplaceOption.Replace);
		}
		
		return status;
	}
}
