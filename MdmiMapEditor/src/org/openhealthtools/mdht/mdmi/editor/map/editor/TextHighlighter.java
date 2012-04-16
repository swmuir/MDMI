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
package org.openhealthtools.mdht.mdmi.editor.map.editor;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

/** Highlight some text */
public class TextHighlighter {
	
	/** Highlight text by setting background color */
	public static class TextHighlighterPainter extends LayeredHighlighter.LayerPainter {	
		private Color m_color;

		public TextHighlighterPainter(Color c) {
			m_color = c;
		}

		public Color getHighlightColor() {
			return m_color;
		}

		public void setHighlightColor(Color color) {
			m_color = color;
		}

		@Override
		public void paint(Graphics g, int offs0, int offs1, Shape bounds,
				JTextComponent c) {
			// Do nothing: this method will never be called
		}

		@Override
		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
				JTextComponent c, View view) {
			g.setColor(m_color == null ? c.getSelectionColor() : m_color);

			Rectangle rect = getShapeBounds(offs0, offs1, bounds, view);

			// draw box
			g.fillRect(rect.x, rect.y, rect.width, rect.height);

			return rect;
		}

		/**
		 * @param offs0
		 * @param offs1
		 * @param bounds
		 * @param view
		 * @return
		 */
		protected Rectangle getShapeBounds(int offs0, int offs1, Shape bounds,
				View view) {
			Shape shape;
			if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
				shape = bounds;
			} else {
				try {
					shape = view.modelToView(offs0, Position.Bias.Forward,
							offs1, Position.Bias.Backward,
							bounds);
				} catch (BadLocationException e) {
					return null;
				}
			}

			Rectangle rectangle = (shape instanceof Rectangle) ? 
					(Rectangle)shape : shape.getBounds();
					return rectangle;
		}
	}


	/** Highlighter that underlines text with a wavy line in a particular color */
	public static class UnderlineHighlightPainter extends TextHighlighterPainter {

		public UnderlineHighlightPainter(Color c) {
			super(c);
		}


		@Override
		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
				JTextComponent c, View view) {

			g.setColor(getHighlightColor() == null ? c.getSelectionColor() : getHighlightColor());

			Rectangle rect = getShapeBounds(offs0, offs1, bounds, view);

			// draw two dotted lines one below the other, offset by two
			// --__--__--__--__--__
			FontMetrics fm = c.getFontMetrics(c.getFont());
			int baseline = rect.y + rect.height - fm.getDescent() + 1;
			int xW = rect.x + rect.width -2;
//			g.drawLine(rect.x, baseline, rect.x + rect.width, baseline);
//			g.drawLine(rect.x, baseline + 1, rect.x + rect.width,
//					baseline + 1);
			for (int x1 = rect.x-1; x1 < xW; x1 += 4) {
				g.drawLine(x1, baseline, x1+2, baseline);
				g.drawLine(x1 +2, baseline+1, x1+2 +2, baseline+1);
			}

			return rect;
		}

	}
}

