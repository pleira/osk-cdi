/*
* plotpos.java
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
*/

package org.opensimkit.console;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

class plotpos extends JLabel {
    
    File configfile = new File("config.txt");   // definition of configuration file
    int count=0;                                // counter for elements per line
    int a,b,c;                                  // columns of output file with needed information
    String[] splittArray;                       // array for elements of line
    String outputfile;                          // string representing name of output file
    double[] Long;                              // arrays with all ...
    double[] Lat;                               // ... positions in orbit
    int x_pixel;                                // pixel information for ...
    int y_pixel;                                // ... painting purposes
    int i;                                      // number of lines in output file
    boolean ln;                                 // booleans for parameter ...
    boolean lt;                                 // ... existence check ...
    boolean check=false;                        // ... in output file
    
    public void transformationPixel(int y) {    // transforming position to pixel information
        
        if (Long[y] <= 3.14159) {
            x_pixel = (int) (((Long[y]+3.14159) / 6.283815) * 720);
        }
        else{
              x_pixel=(int) (((Long[y]-3.14159) / 6.283815) * 720);
            }
        
        y_pixel=(int) (((1.57080-Lat[y])/3.14159) * 360)+1;
        
    }
 
    @Override
    public void paint(Graphics g) {             // paint orbit data over Earth map
        try {
            i=0;                                // reset number of lines for output file
            Image image = null;                 // defining image
            File file = new File("../mmi/figures/earth.jpg");
                                                // path to Earth map
            image = ImageIO.read(file);         // setting image to Earth map
            g.drawImage(image, TOP, TOP, labelFor);
                                                // drawing Earth map
            g.setColor(Color.red);              // setting color to red
            ln=false;                           // resetting booleans
            lt=false;                           
            this.readout();                     // readout output file
            if (ln==true && lt==true) {         // draw orbit if data available
                for (int m=11; m<(i-1); m++) {
                    this.transformationPixel(m);
                    g.fillOval(x_pixel, y_pixel, 2, 2);
                    }
            } else if (check==false) {          // throw error if data not available
                JOptionPane.showMessageDialog(this, "Needed parameters not provided by selected outputfile!", "Error", JOptionPane.ERROR_MESSAGE);
                check=true;
            }
            g.create();                         // create drawing
        } catch (IOException ex) {
            Logger.getLogger(plotpos.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void readout() {                     // readout output file
        if(!configfile.exists()) {              // check whether configuration file exists
            JOptionPane.showMessageDialog(this, "Exiting. Please define output file!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
        
        try {
            BufferedReader of = new BufferedReader(new FileReader(configfile));
            outputfile = of.readLine();         // read name of output file
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File output = new File (outputfile);
        if (!output.exists()) {                 // check whether output file exists
            JOptionPane.showMessageDialog(this, "Output file does not exist, please redefine!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {  
                try {
                    BufferedReader in = new BufferedReader(new FileReader(outputfile));
                                                // read from outputfile
                    String zeile = null;        // string to store content of a line                                      // integer representing current line
                    while ((zeile = in.readLine()) != null) {
                        i++;
                        if (i==8) {
                            splittArray = zeile.split("\\s+");      // split content of line at positions with one or several spaces
                            count = splittArray.length;             // count number of elements
                        } 
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int z=0; z<count; z++) {   // check whether needed information available
                    String compare;
                    compare = splittArray[z];
                    if (compare.equals("22_STRUCTURE-SCPOSLON")) {
                        a = z-1;
                        ln=true;
                    }
                    else if (compare.equals("22_STRUCTURE-SCPOSLAT")) {
                        b = z-1;
                        lt=true;
                    }
                    else if (compare.equals("22_STRUCTURE-SCPOSALT")) {
                        c = z-1;
                    }
                }
                if (ln==true && lt==true) {     // readout data when available
                    try {                        
                        BufferedReader in2 = new BufferedReader(new FileReader(outputfile));
                        String zeile = null;    // string to store content of a line
                        int k=0;                // resetting counter
                        Long = new double[i+1]; // defining arrays ...
                        Lat = new double[i+1];  // ... for data
                        while((zeile = in2.readLine()) != null) {
                            k++;
                            if ((k>9 && (k<i-1))) { 
                                splittArray = zeile.split("\\s+");
                                                // split content of line at positions with one or several spaces                               
                                Long[k]= Double.valueOf( splittArray[a] );
                                Lat[k]= Double.valueOf( splittArray[b] );
                                                // storing orbit data in arrays
                                }
                            }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
    }

    public boolean pmav() {             // method returning information whether data available
        boolean ck=true;
        if (ln==false || lt==false) {
            ck=false;
        }
        return ck;
    }
}
