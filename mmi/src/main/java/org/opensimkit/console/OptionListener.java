/*
* OptionListener.java
*
*  This class opens the configuration window for plotting issues
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2009-09-11
*      File written by Michael Fritz
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*/

package org.opensimkit.console;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;



public class OptionListener implements ActionListener {

    public void actionPerformed(ActionEvent arg0) {
        try {
            options frame = new options();
            frame.createframe();
        } catch (IOException ex) {
            Logger.getLogger(OptionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    


}
