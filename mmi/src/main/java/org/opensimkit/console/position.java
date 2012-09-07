/*
* position.java
*
*  This class opens the configuration window for plotting issues
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2009-11-23
*      File written by Roland Weil and Michael Fritz
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*  2010-07-29
*      Added functionality for dynamical plots by Michael Fritz
* 
*/

package org.opensimkit.console;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;



public class position extends JFrame implements ActionListener {

    
    Graphics g = new Graphics() {                   // declaring graphics    

        @Override
        public Graphics create() {
            return g;
        }

        @Override
        public void translate(int arg0, int arg1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Color getColor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setColor(Color arg0) {
            
        }

        @Override
        public void setPaintMode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setXORMode(Color arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Font getFont() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFont(Font arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FontMetrics getFontMetrics(Font arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Rectangle getClipBounds() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clipRect(int arg0, int arg1, int arg2, int arg3) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setClip(int arg0, int arg1, int arg2, int arg3) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Shape getClip() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setClip(Shape arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void drawLine(int arg0, int arg1, int arg2, int arg3) {
            
        }

        @Override
        public void fillRect(int arg0, int arg1, int arg2, int arg3) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clearRect(int arg0, int arg1, int arg2, int arg3) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void drawOval(int arg0, int arg1, int arg2, int arg3) {
            
        }

        @Override
        public void fillOval(int arg0, int arg1, int arg2, int arg3) {
            
        }

        @Override
        public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void drawString(String arg0, int arg1, int arg2) {
            
        }

        @Override
        public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
            
            return true;
        }

        @Override
        public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, ImageObserver arg5) {
            
            return true;
        }

        @Override
        public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3, ImageObserver arg4) {
            
            return true;
        }

        @Override
        public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, Color arg5, ImageObserver arg6) {
            
            return true;
        }

        @Override
        public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
            
            return true;
        }

        @Override
        public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9, ImageObserver arg10) {
            
            return true;
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    

    public void actionPerformed(ActionEvent arg0) { // method creating frame
        
        if (((JMenuItem)arg0.getSource()).getText().equals("Plot position")) {
                                                    // execute when corresponding button pushed
            boolean ck=true;                        // defining boolean
            position frame = new position();        // declaring frame
            plotpos klasse = new plotpos();         // declaring class
            klasse.paint(g);                        // painting
            ck=klasse.pmav();                       // check whether data available
            if (ck == true) {                       // display frame when data available
                try {
                    // display frame when data available
                    frame.add(klasse);
                    frame.setLocation(250, 250);
                    frame.setSize(720,360);
                    frame.setVisible(true);
                    frame.setSize(720+frame.getInsets().left+frame.getInsets().right,360+frame.getInsets().top+frame.getInsets().bottom);
                    Thread.currentThread().sleep(400);
                    frame.setResizable(false);
                } catch (InterruptedException ex) {
                    Logger.getLogger(position.class.getName()).log(Level.SEVERE, null, ex);
                }
                Runnable t=new updateplotpos(klasse, frame, g);
                new Thread(t).start();
            }
        }
    }   
        
}