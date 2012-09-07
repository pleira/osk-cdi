/*
* GraphListener.java
*
*  This class opens the selection window
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2009-08-15
*      File written by Michael Fritz
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*/

package org.opensimkit.console;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fritz
 */
public class GraphListener implements ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
        try {
            plot frame = new plot(); 
            frame.Select();

        } catch (InterruptedException ex) {
            Logger.getLogger(GraphListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }

}
