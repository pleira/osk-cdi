/*
* updateplots.java
*
*  This class updates the graph plots
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fritz
 */
class updateplots extends Thread implements Runnable{
    
    plot existplot;

    public updateplots(plot plot) {
        existplot = plot;
    }
    
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(updateplots.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(existplot.isVisible()) {
            try {
                existplot.graph(false);
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(updateplots.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
