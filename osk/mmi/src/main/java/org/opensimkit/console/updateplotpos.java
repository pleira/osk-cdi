/*
* updateplotpos.java
*
*  This class updates the Position plot over Earth
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2010-07-29
*      File written by Michael Fritz
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*/

package org.osk.console;

import java.awt.Graphics;

/**
 *
 * @author fritz
 */
class updateplotpos extends Thread implements Runnable{
    
    plotpos plot;
    position tmp;
    Graphics graph;
    
    public updateplotpos(plotpos klasse, position frame, Graphics g) {
        plot=klasse;
        tmp = frame;
        graph=g;
    }
  
    @Override
    public void run() {
         this.updateposplot();   
    }
    
    public void updateposplot() {
        while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    System.out.println("Java exception");
                }
                plot.paint(graph);
                plot.repaint();
        }
    }
}
