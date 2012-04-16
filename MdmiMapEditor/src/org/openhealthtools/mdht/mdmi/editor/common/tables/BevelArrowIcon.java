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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.UIManager;
 
/**
  * This code was available on the web
  */
public class BevelArrowIcon implements Icon
{
    // direction
    public static final int UP    = 0;
    public static final int DOWN  = 1;

    private static final int DEFAULT_SIZE = 10;

    private Color _edge1;
    private Color _edge2;
    private Color _fill;
    private int _size;
    private int _direction;

    /**
      * Constructor
      *
      * @param int  The direction of the arrow, up or down
      * @param boolean   Is the arrow raised
      */

    public BevelArrowIcon(int direction, boolean isRaisedView)
    {
       if (isRaisedView)
       {
          init( UIManager.getColor("controlLtHighlight"),
                UIManager.getColor("controlDkShadow"),
                UIManager.getColor("control"),
                DEFAULT_SIZE, direction);
       }
       else
       {
          init( UIManager.getColor("controlShadow"),
                UIManager.getColor("controlLtHighlight"),
                UIManager.getColor("control"),
                DEFAULT_SIZE, direction);
       }
    }

    /**
      * Constructor
      *
      * @param Color  Color of one icon edge
      * @param Color  Color of another icon edge
      * @param Color  The fill color
      * @param int    The size of the icon
      * @param int    The direction of the arrow, up or down
      */

    public BevelArrowIcon(Color edge1, Color edge2, Color fill,
                          int size, int direction)
    {
        init(edge1, edge2, fill, size, direction);
    }

    /**
      * Paint the icon in the column header
      *
      * @param Component  Component the icons will be drawn on
      * @param Graphics   Graphics for the icon
      * @param int        The x coordinates
      * @param int        The y coordinates
      */

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        switch (_direction)
        {
            case DOWN: drawDownArrow(g, x, y); break;
            case   UP: drawUpArrow(g, x, y);   break;
        }
    }

    /**
      * Retrieve the width of the icon
      *
      * @return int Width of icon
      */

    public int getIconWidth()
    {
        return _size;
    }

    /**
      * Retrieve the height of the icon
      *
      * @return int Height of icon
      */

    public int getIconHeight()
    {
        return _size;
    }


    private void init(Color edge1, Color edge2, Color fill,
                      int size, int direction)
    {
        this._edge1 = edge1;
        this._edge2 = edge2;
        this._fill = fill;
        this._size = size;
        this._direction = direction;
    }

    private void drawDownArrow(Graphics g, int x0, int y0)
    {
       //    x1, y1       x2, y2
       //       ------------
       //       \          /
       //        \        /
       //         \      /
       //          \    /
       //           \  /
       //            \/
       //          x3, y3
       int x1 = x0 + 1;
       int y1 = y0 + 1;
       
       int x2 = x1 + _size - 3;
       int y2 = y1;
       
       int x3 = (x1 + x2)/2;
       int y3 = y0 + _size - 3;
       
       g.setColor(_fill);
       g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
       
       g.setColor(_edge1);
       g.drawLine(x1, y1, x2, y2);
       g.drawLine(x1, y1, x3, y3);
       
       g.setColor(_edge2);
       g.drawLine(x3+1, y3, x2, y2);
    }

    private void drawUpArrow(Graphics g, int x0, int y0)
    {
       //          x3, y3
       //            /\
       //           /  \
       //          /    \
       //         /      \
       //        /        \
       //       /          \
       //       ------------
       //    x1, y1       x2, y2

       int x1 = x0 + 1;
       int y1 = y0 + _size - 3;
       
       int x2 = x1 + _size - 3;
       int y2 = y1;
       
       int x3 = (x1 + x2)/2;
       int y3 = y0 + 1;
       
       g.setColor(_fill);
       g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
       
       g.setColor(_edge1);
       g.drawLine(x1, y1, x3, y3);
       
       g.setColor(_edge2);
       g.drawLine(x1, y1, x2, y2);
       g.drawLine(x3+1, y3, x2, y2);
    }
}
