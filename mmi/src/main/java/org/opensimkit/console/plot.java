/*
* plot.java
*
*  This class has been created to plot simulation output
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2009-08-15
*      File written by Michael Fritz
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*  2009-11-18
*      Time scaling bug fixed
*
*/

package org.opensimkit.console;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


class plot extends JFrame implements ActionListener {       // class definition
    
    JFrame selframe = new JFrame("Variables Browser");      // definition of selection window 
    JButton testJButton = new JButton("Create graph");      // definition of button in selection window
    JCheckBox[] cb;                                         // checkbox array
    int count=0;                                            // counter for elements per line
    String[] splittArray;                                   // array for elements of line
    String outputfile;                                      // name of output file
    File configfile = new File("config.txt");               // definition of configuration file
    ChartFrame frame = new ChartFrame("Visualization", null);
    FlowLayout flow = new FlowLayout();
    Panel chartPanel = new Panel(flow);
    
    public void Select() throws InterruptedException {      // method for selection window
        if(!configfile.exists()) {                          // check whether configuration file exists
            JOptionPane.showMessageDialog(selframe, "Exiting. Please define output file!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                BufferedReader of = new BufferedReader(new FileReader(configfile));
                outputfile = of.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File output = new File (outputfile);
            if (!output.exists()) {
                JOptionPane.showMessageDialog(selframe, "Output file does not exist, please redefine!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(outputfile));
                                                                    // read from outputfile
                    String zeile = null;                            // string to store content of a line
                    int i=0;                                        // integer representing current line
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
                selframe.setLayout(new GridLayout(count-1,1));      // set window layout depending on elements in output file
                cb = new JCheckBox[count];                          // set size of checkbox array
                for (int j=2; j<count; j++) {                       // read from third position as the first two ones should be "Mission Time"
                    cb[j] = new JCheckBox(splittArray[j], false);   // set label and state for checkbox j
                    selframe.add(cb[j]);                            // add checkbox j to window
                }
                if (count>2) {                                      // more than two elements are necessary to create a graph
                    testJButton.addActionListener(this);            // add action listener
                    selframe.add(testJButton);                      // add button to window
                    selframe.setLocation(150, 150);                 // set window location
                    selframe.pack();                                // size window
                    selframe.setVisible(true);                      // set visibility of window
                } else {
                    JOptionPane.showMessageDialog(selframe, "Simulation not yet started!", "Error", JOptionPane.ERROR_MESSAGE);
                                                                    // error messsage
                }
            }
        }
    }
    
    public void actionPerformed(ActionEvent e)              // method for pushing button
    {
        try {
            boolean check = false;
            for (int y=2; y<count; y++) {
                if (cb[y].isSelected() == true) {           // if any checkbox is selected ...
                    check = true;                           // ... set boolean to true
                }
            }
            if (check == true) {                            // if at least one checkbox is selected ...
                graph(true);                                    // ... create graph
            } else {
                JOptionPane.showMessageDialog(selframe, "No value selected!", "Error", JOptionPane.ERROR_MESSAGE);
                                                            // if none is selected, print error message
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(plot.class.getName()).log(Level.SEVERE, null, ex);
        }
        Runnable t=new updateplots(this);
        new Thread(t).start();
    }
    
    private JFreeChart chart;                               // chart
    XYSeries[] series;                                      // data series
    XYSeriesCollection dataset;                             // dataset
    String[] splittValues;                                  // array with values of each line
    boolean state[] = new boolean[count] ;                  // array with states of checkboxes
    
    public void graph(boolean newgraph) throws InterruptedException {       // method for graph window
        
    try {
                series = new XYSeries[count];               // set size of series
                double stepsize = 0.0;                      // time period between two simulation steps
                for (int x=2; x<count; x++) {               // read values
                    BufferedReader in1 = new BufferedReader(new FileReader(outputfile));  
                                                            // read from outputfile
                    String zeile = null;                    // string to store line
                    String check = null;                    // string to store states of checkboxes
                    int i=0;                                // counter for lines
                    int s=0;                                // integer to identify last line
                    double time = 0.0;                      // simulation time
                    dataset = new XYSeriesCollection();     // dataset
                    series[x] = new XYSeries(splittArray[x]);
                                                            // Array of series to be plot
                    String time0;                           // points of time ...
                    String time1;                           // ... to calculate stepsize
                    if (x==2) {                             // condition to do just once
                        while ((zeile = in1.readLine()) != null) {
                            i++;
                            double time00=0.0;
                            double time01=0.0;
                            if (i==9) {
                                time0 = zeile.substring(17,23);
                                time00 = Double.valueOf(time0).doubleValue();
                                                            // read first point of time and convert to double
                            }
                            if (i==10) {
                                time1 = zeile.substring(17,23);
                                time01 = Double.valueOf(time1).doubleValue();
                                                            // read second point of time and convert to double                                
                                stepsize = (time01 -time00);
                                                            // calculate step size
                            }
                        }
                    }
                    i=0;                                    // reset i
                    BufferedReader in2 = new BufferedReader(new FileReader(outputfile));
                                                            // read from outputfile once again
                    while ((zeile = in2.readLine()) != null) {
                        i++;
                        if (i>8) {
                            check = zeile;
                            int length = check.length();
                            if (length != 0) {              // check whether the current line is empty
                                if (s==0) {
                                    splittValues = zeile.split("\\s+");
                                                                // split line at one or several spaces
                                    double value = Double.parseDouble(splittValues[x-1]);
                                                            // convert string to double
                                    series[x].add(time, value); // add value to series
                                    time = time + stepsize;     // increase time
                                }
                            }
                            else {
                                s=1;
                            }
                        }
                    }
                }
	} catch (IOException e) {
		e.printStackTrace();
	}
    for (int n=2; n<count; n++) {
        if (cb[n].isSelected() == true)
            dataset.addSeries(series[n]);                   // add series to dataset when checkbox selected
    }
    
    String title = "";
    String Ylabel = "";
    String Xlabel = "time [s]";
    chart = ChartFactory.createXYLineChart(title, Xlabel, Ylabel, dataset, PlotOrientation.VERTICAL, true, true, true);
                                                            // create chart
    frame = new ChartFrame("Visualization", chart);
                                                                    // create window with chart
    flow = new FlowLayout();
    flow.setVgap(20);
    this.setLayout(flow);
    chartPanel = new Panel(flow);

    chartPanel.add(frame.getChartPanel());
    this.setContentPane(chartPanel);
    if (newgraph) {
        this.setLocation(150,150);
        this.setSize(800,700);
        }
    this.pack();
    this.setVisible(true);
    }
}
