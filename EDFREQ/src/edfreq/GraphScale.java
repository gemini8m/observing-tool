/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

package edfreq;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 * @author Dennis Kelly ( bdk@roe.ac.uk )
 */
public class GraphScale extends JPanel implements ChangeListener
{
   private static final int MIN_LABEL_SPACING = 60;

   private int orientation;
   private Rectangle tickRect = new Rectangle();
   private Rectangle axisRect = new Rectangle();
   private double majorIncrement;
   private double minorIncrement;
   private double maximum;
   private double minimum;
   private boolean inverted = false;
   private int trackBuffer = 0;
   private double pixelsPerValue;
   private Image buffer = null;
   private Graphics ig;
   private int xSize = 0;
   private int ySize = 0;
   private double halfrange;
   private int length;
   private int exponent;
   private double redshift;
   private double restMinimum;
   private double restMaximum;
   private double restHalfrange;


   public GraphScale ( double minimum, double maximum, 
     double majorIncrement, double minorIncrement, double redshift,
     int exponent, int length, int orientation )
   {
      super();

      setLayout ( null );

      restMinimum = minimum;
      restMaximum = maximum;
      this.minimum = minimum * ( 1.0 + redshift );
      this.maximum = maximum * ( 1.0 + redshift );
      this.majorIncrement = majorIncrement;
      this.minorIncrement = minorIncrement;
      this.redshift = redshift;
      this.exponent = exponent;
      this.length = length;
      this.orientation = orientation;

      this.pixelsPerValue = ((double)length) / ( this.maximum - this.minimum );
      halfrange = ( this.maximum - this.minimum ) / 2.0;
      restHalfrange = ( restMaximum - restMinimum ) / 2.0;

      calculateTickRect();
      calculateAxisRect();
      setPreferredSize ( new Dimension ( axisRect.width, 
        axisRect.height ) );
   }

   public void setOrientation ( int orientation )
   {
      this.orientation = orientation;
   }


   public void setRedshift ( double redshift )
   {
      this.redshift = redshift;
      this.minimum = restMinimum * ( 1.0 + redshift );
      this.maximum = restMaximum * ( 1.0 + redshift );
      pixelsPerValue = ((double)length) / ( maximum - minimum );
      halfrange = ( maximum - minimum ) / 2.0;
      repaint();
   }


   protected int getTickLength() 
   {
      return 8;
   }

   protected void calculateTickRect() 
   {
      if ( orientation == JSlider.HORIZONTAL ) 
      {
         tickRect.x = 0;
         tickRect.y = 0;
         tickRect.width = length;
         tickRect.height = getTickLength();
      }
      else 
      {
         tickRect.x = 20;
         tickRect.y = 0;
         tickRect.width = getTickLength();
         tickRect.height = length;
      }
   }

   protected void calculateAxisRect() 
   {
      if ( orientation == JSlider.HORIZONTAL ) 
      {
         axisRect.x = tickRect.x;
         axisRect.y = tickRect.y;
         axisRect.width = tickRect.width;
         axisRect.height = tickRect.height + 20;
      }
      else 
      {
         axisRect.width = tickRect.width + 20;
         axisRect.height = tickRect.height;
         axisRect.x = tickRect.x - 20;
         axisRect.y = tickRect.y;
      }
   }

   public void paintTicks(Graphics g)
   {
      Rectangle tickBounds = tickRect;
      int i;
      double maj;
      double min;
      double max;

      int w = tickBounds.width;
      int h = tickBounds.height;
      int tickHeight;

      g.setColor(getBackground());
      g.fillRect(tickBounds.x, tickBounds.y, tickBounds.width, 
        tickBounds.height);  
      g.setColor(Color.black);

/* Find values corresponding to first major and minor tick marks */

      maj = majorIncrement * (double)
        ( (int) ( (minimum+majorIncrement) / majorIncrement ) );
      min = minorIncrement * (double)
        ( (int) ( (minimum+minorIncrement) / minorIncrement ) );

      if ( orientation == JSlider.HORIZONTAL ) 
      {

         int xPos = 0;
         int xPosPrevious = 0;

         if ( minorIncrement > 0.0 ) 
         {
            while ( min <= maximum ) 
            {
               xPos = xPositionForValue ( min );
               g.drawLine ( xPos, 0, xPos, tickBounds.height / 2 - 1 );
               min += minorIncrement;
            }
         }

         if ( majorIncrement > 0 ) 
         {

            while ( maj <= maximum ) 
            {
               xPos = xPositionForValue ( maj );

               g.drawLine ( xPos, 0, xPos, tickBounds.height - 2 );

               if((xPos - xPosPrevious) >= MIN_LABEL_SPACING) {
                  drawLabelX ( g, xPos, tickBounds.height, maj, exponent );

                  xPosPrevious = xPos;
               }

               maj += majorIncrement;
            }
         }

      }
      else 
      {

         int yPos = 0;

         if ( minorIncrement > 0 ) 
         {
            while ( min <= maximum ) 
            {
               yPos = yPositionForValue ( min );

               g.drawLine( tickBounds.x + tickBounds.width / 2 - 1, 
                 yPos, 
                 tickBounds.x + tickBounds.width - 2, yPos );

               min += minorIncrement;
            }
         }

         if ( majorIncrement > 0 ) 
         {

            while ( maj <= maximum ) 
            {
               yPos = yPositionForValue ( maj );

               g.drawLine ( tickBounds.x, yPos,  
                 tickBounds.x + tickBounds.width - 2, yPos );
               drawLabelY ( g, tickBounds.x, yPos, maj, exponent );

               maj += majorIncrement;
            }
         }
      }
   }


   private double log10 ( double value )
   {
      double log10e = 0.4342944819033;

      return ( log10e * Math.log ( value ) );
   }


   private void drawLabelX ( Graphics g, int x, int y, double value, 
     int exponent )
   {
      Graphics2D g2;
      Rectangle2D bounds;
      FontRenderContext frc;

      String svalue;
      double tvalue;

      if ( exponent != 0 )
      {
         tvalue = value / Math.pow ( 10.0, exponent );
         svalue = "" + tvalue + "E" + exponent;
      }
      else
      {
         svalue = "" + value;
      }

      g2 = (Graphics2D)g;
      frc = g2.getFontRenderContext();
      bounds = g2.getFont().getStringBounds ( svalue, frc );
      int xpos = x - ((int)bounds.getWidth()) / 2;
      int ypos = y + (int)bounds.getHeight();
      g.drawString ( svalue, xpos, ypos );
   }



   private void drawLabelY ( Graphics g, int x, int y, double value, 
     int exponent )
   {
      Graphics2D g2;
      Rectangle2D bounds;
      FontRenderContext frc;

      String svalue;
      double tvalue;

      if ( exponent != 0 )
      {
         tvalue = value / Math.pow ( 10.0, exponent );
         svalue = "" + tvalue + "E" + exponent;
      }
      else
      {
         svalue = "" + value;
      }

      g2 = (Graphics2D)g;
      frc = g2.getFontRenderContext();
      bounds = g2.getFont().getStringBounds ( svalue, frc );
      int xpos = x - (int)bounds.getWidth();
      int ypos = y + ((int)bounds.getHeight()) / 2;
      g.drawString ( svalue, xpos, ypos );
   }


   protected int xPositionForValue ( double value )
   {
      int trackLeft = tickRect.x;
      int trackRight = tickRect.x + (tickRect.width - 1);

      return positionForValue ( value, trackLeft, trackRight );
   }

   protected int yPositionForValue ( double value )
   {
      int trackTop = tickRect.y;
      int trackBottom = tickRect.y + (tickRect.height - 1);
      inverted = true;
      return positionForValue ( value, trackTop, trackBottom );
   }

   protected int positionForValue ( double value, int trackStart, 
     int trackEnd )
   {
      int position;

      if ( !inverted ) 
      {
         position = trackStart;
         position += Math.round ( pixelsPerValue * ( value - minimum ) );
      }
      else 
      {
         position = trackEnd;
         position -= Math.round ( pixelsPerValue * ( value - minimum ) );
      }

      position = Math.max ( trackStart, position );
      position = Math.min ( trackEnd, position );

      return position;
   }


   public void paintComponent ( Graphics g )
   {
      int j;

      if ( buffer == null )
      {
         xSize = axisRect.width;
         ySize = axisRect.height; 
         buffer = createImage ( xSize, ySize );

	 // added by MFO, 16 November 2001
	 if(buffer == null) {
            return;
	 }

	 ig = buffer.getGraphics();
      }

      ig.setColor ( getBackground() );
      ig.fillRect ( 0, 0, xSize, ySize );
      ig.setColor ( getForeground() );

      paintTicks ( ig );
      g.drawImage ( buffer, 0, 0, null );
   }


   public void stateChanged ( ChangeEvent e )
   {
      double value;
      int j;

      value = EdFreq.SLIDERSCALE * 
        (double) ( ((JSlider)e.getSource()).getValue() );
      restMinimum = value - restHalfrange;
      restMaximum = value + restHalfrange;
      minimum = restMinimum * ( 1.0 + redshift );
      maximum = restMaximum * ( 1.0 + redshift );

      repaint();

   }

}
