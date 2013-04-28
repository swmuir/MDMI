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
package org.openhealthtools.mdht.mdmi.editor.map.tools;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.RepaintManager;

/** Utility for printing a swing component on a single page */
public class PrintUtilities implements Printable {
	public static int s_1quarterInch  = 18;	// 1/4 inch
	public static int s_halfInch      = 36;	// 1/2 inch
	public static int s_3quartersInch = 54;	// 3/4 inch
	public static int s_oneInch       = 72;	// 1 inch
	
	private Component m_componentToBePrinted;
	private String    m_title;

	/** Print portrait or landscape mode, depending on width:height */
	public static void printComponent(Component c, String title) throws PrinterException {
		int orientation = PageFormat.PORTRAIT;
		Insets margin = new Insets(s_3quartersInch, s_1quarterInch,
				s_halfInch, s_1quarterInch);
		int width = c.getWidth();
		int height = c.getHeight();
		if (width > height) {
			// rotate
			orientation = PageFormat.LANDSCAPE;
		}
		PrintUtilities pu = new PrintUtilities(c, title);
		pu.print(orientation, margin);
	}

	public PrintUtilities(Component componentToBePrinted) {
		this.m_componentToBePrinted = componentToBePrinted;
	}

	public PrintUtilities(Component componentToBePrinted, String title) {
		this.m_componentToBePrinted = componentToBePrinted;
		this.m_title = title;
	}
	
	/** Set a title for top of page */
	public void setTitle(String title) {
		this.m_title = title;
	}

	/** Print the component.
	 * 
	 * @param orientation	The orientation - PageFormat.PORTRAIT or PageFormat.LANDSCAPE
	 * @param margin	The top, bottom, left and right margins (in points, 72pts = 1 inch)
	 * @throws PrinterException
	 */
	public void print(int orientation, Insets margin) throws PrinterException {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		PageFormat format = printJob.defaultPage();
		format.setOrientation(orientation);
		Paper paper = format.getPaper();
		double paperWidth = paper.getWidth();
		double paperHeight = paper.getHeight();
		// set margins
		if (orientation == PageFormat.LANDSCAPE) {
			// rotated, so use margin.top for left side, and margin.right for top
			paper.setImageableArea(margin.top, margin.right,
					paperWidth-margin.top-margin.bottom,	// width
					paperHeight-margin.right-margin.left);	// height
		} else {
			paper.setImageableArea(margin.left, margin.top,
					paperWidth-margin.left-margin.right,	// width
					paperHeight-margin.top-margin.bottom);	// height
		}
		format.setPaper(paper);
		printJob.setPrintable(this, format);
		if (printJob.printDialog()) {
			printJob.print();
		}
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			return(NO_SUCH_PAGE);
		} else {
			Graphics2D g2d = (Graphics2D)g;
			FontMetrics fm = g.getFontMetrics();
			double imageX = pageFormat.getImageableX();
			double imageY = pageFormat.getImageableY();
			// adjust for title
			if (m_title != null && m_title.length() > 0) {
				imageY += 2*fm.getHeight();
			}
			//Set us to the upper left corner
			g2d.translate(imageX, imageY);

			//We need to scale the image properly so that it fits on one page.
			double imageWidth = pageFormat.getImageableWidth();
			double imageHeight = pageFormat.getImageableHeight();
			double xScale = imageWidth / m_componentToBePrinted.getWidth();
			double yScale = imageHeight / m_componentToBePrinted.getHeight();

			// Maintain the aspect ratio by taking the min of those 2 factors and
			// using it to scale both dimensions.
			double aspectScale = Math.min(xScale, yScale);
			g2d.scale(aspectScale, aspectScale);

			disableDoubleBuffering(m_componentToBePrinted);
			m_componentToBePrinted.paint(g2d);
			// label
			if (m_title != null) {
				// scale font size
				Font font = g.getFont();
				g.setFont( font.deriveFont((float)(font.getSize()/aspectScale)) );
				int textWidth = fm.stringWidth(m_title);
				int xPos = (int)((imageWidth-textWidth)/2/aspectScale);
				int yPos = -fm.getHeight();
				g.drawString(m_title, xPos, yPos);
			}
			
			enableDoubleBuffering(m_componentToBePrinted);
			return(PAGE_EXISTS);

		}
	}

	public static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	public static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}