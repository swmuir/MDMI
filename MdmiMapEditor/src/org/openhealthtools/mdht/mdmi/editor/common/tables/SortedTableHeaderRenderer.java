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
package org.openhealthtools.mdht.mdmi.editor.common.tables;


import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class SortedTableHeaderRenderer extends DefaultTableCellRenderer // implements TableCellRenderer
{
	private static final Icon UPARROW    = new BevelArrowIcon(BevelArrowIcon.UP, false);
	private static final Icon DOWNARROW  = new BevelArrowIcon(BevelArrowIcon.DOWN, false);
	private TableSorter m_sorter;
	private JLabel m_sizeLabel = new JLabel(" ");

	public SortedTableHeaderRenderer( TableSorter sorter )
	{
		this.m_sorter = sorter;
		setOpaque(true);
		setForeground(UIManager.getColor("TableHeader.foreground"));
		setBackground(UIManager.getColor("TableHeader.background"));
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setFont( UIManager.getFont("TableHeader.font"));
		setHorizontalTextPosition( SwingConstants.LEFT );
		this.setIconTextGap( 2 );
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		setText( value.toString() );
		int convertColumn = table.convertColumnIndexToModel( column );
		if( m_sorter != null && m_sorter.isSortColumn( convertColumn ) )
		{
			if( m_sorter.isAsscending( convertColumn  ) )
			{
				setIcon(UPARROW);
			}
			else
			{
				setIcon(DOWNARROW);
			}
		}
		else
		{
			setIcon( null );
		}
		return this;
	}

	@Override
	public Dimension getPreferredSize()
	{
		if( getText().equals( "" ))
		{
			return( m_sizeLabel.getPreferredSize() );
		}
		return( super.getPreferredSize() );
	}

	@Override
	public Dimension getMaximumSize()
	{
		if( getText().equals( "" ))
		{
			return( m_sizeLabel.getMaximumSize() );
		}
		return( super.getMaximumSize() );
	}

	@Override
	public Dimension getMinimumSize()
	{
		if( getText().equals( "" ))
		{
			return( m_sizeLabel.getMinimumSize() );
		}
		return( super.getMinimumSize() );
	}
}
