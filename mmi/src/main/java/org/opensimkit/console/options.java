/*
* OptionListener.java
*
*  This class has been created to choose the outputfile for visualization issues
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

import java.awt.TextField;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;


public class options extends JFrame { // class definition
    
        File configfile = new File("config.txt");               // definition of configuration file
        JFrame optframe = new JFrame("Options");                // definition of configuration window
        TextField tf;                                           // definition of text field
        JFileChooser chooser = new JFileChooser();              // file chooser

        public void createframe() throws IOException {          // method for creation of options window
                        
            if(!configfile.exists()) {                          // Create configuration file when not existing yet
                try {
                    configfile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(OptionListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            chooser.addChoosableFileFilter(new FileFilter() {   // add filter to file chooser
                
            public boolean accept(File f) {                     // set filter to plain text files
                if (f.isDirectory()) return true;
                return f.getName().toLowerCase().endsWith(".txt");
            }
            
            public String getDescription () { return "text files"; }  });
            
            chooser.setMultiSelectionEnabled(false);            // multi selection not enabled
            if (chooser.showOpenDialog(optframe) == JFileChooser.APPROVE_OPTION) {
                pushedbutton();                                 // performed action when choosing file
            }
            
    }
    
    public void pushedbutton() {
        try {
            FileWriter fw = new FileWriter(configfile);         // definition of file writer
            BufferedWriter out = new BufferedWriter(fw);        // write to configuration file
            File nf = chooser.getSelectedFile();                // get selected file
            String path = nf.getAbsolutePath();                 // get absolute path of selected file
            out.write(path);                                    // write string to first line of configuration file
            out.close();                                        // close buffered writer
            fw.close();                                         // close file writer
            
        } catch (IOException ex) {
            Logger.getLogger(options.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
}
