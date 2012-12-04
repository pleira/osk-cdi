/*
* ConsoleGUI.java
*
*  This class has been created to create the main MMI window
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2008-12-31
*      File written by Jens Eickhoff
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*  2009-08-15
*      File modified by Michael Fritz
*
*  2009-11-23
*      Minor changes by Michael Fritz
*/

package org.osk.console;


import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultDesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;



//This is the console GUI
public class ConsoleGUI extends JFrame implements ActionListener {
    private JDesktopPane desk;
  String menuCmdLine = "";
  String cWinLine = "";
  ChildTextFrame cWin;
  ChildTextFrame lWin;


  public ConsoleGUI()
  {
    super("OSK Control Console");
    try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    this.desk = new JDesktopPane();
    desk.setDesktopManager(new DefaultDesktopManager());
    setContentPane(desk);
    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

    JMenuBar menubar = new JMenuBar();
    menubar.add(createFileMenu());
    menubar.add(createChartMenu());
    setJMenuBar(menubar);

    cWin = new ChildTextFrame("Cmd Window");
    lWin = new ChildTextFrame("Log Window");

    this.addChild(cWin, 0, 0);
    this.addChild(lWin, 0, 150);

/*
    Runnable r1 = new Runnable()
    {
      public void run() {
        while (!stoprequested) {
          cWinLine = cWin.readLine();
        }
      }
    };
    WinGrabberThread cWinGrabber = new Thread(r1);
*/
//    cWinGrabber = new WinGrabberThread(cWin);
//    cWinGrabber.start();
  }


  public void addChild(JInternalFrame child, int x, int y)
  {
    child.setLocation(x, y);
    child.setSize(400, 150);
    child.setDefaultCloseOperation(
      JInternalFrame.DISPOSE_ON_CLOSE
    );
    desk.add(child);
    child.setVisible(true);
  }


  ChildTextFrame getCWin()
  {
    return cWin;
  }


  String readMenuLine()
  {
    return menuCmdLine;
  }


  void resetMenuLine()
  {
    menuCmdLine = "";
  }


  private JMenu createFileMenu()
  {
    JMenu fMenu = new JMenu("File");
    fMenu.setMnemonic('F');
    JMenuItem mi;
    //Run
    mi = new JMenuItem("Run", 'r');
    setCtrlAccelerator(mi, 'R');
    mi.addActionListener(this);
    fMenu.add(mi);
    //Pause
    mi = new JMenuItem("Pause", 'p');
    setCtrlAccelerator(mi, 'P');
    mi.addActionListener(this);
    fMenu.add(mi);
    //Resume
    mi = new JMenuItem("Resume", 'c');
    setCtrlAccelerator(mi, 'C');
    mi.addActionListener(this);
    fMenu.add(mi);
    //Stop
    mi = new JMenuItem("Stop", 's');
    setCtrlAccelerator(mi, 'S');
    mi.addActionListener(this);
    fMenu.add(mi);
    //Separator
    fMenu.addSeparator();
    //Shutdown
    mi = new JMenuItem("Shutdown", 'q');
    setCtrlAccelerator(mi, 'Q');
    mi.addActionListener(this);
    fMenu.add(mi);
    return fMenu;
  }
  
    
   private JMenu createChartMenu()
  {
    JMenu gMenu = new JMenu("Figures");
    gMenu.setMnemonic('G');
    JMenuItem mi;
    //Options
    mi = new JMenuItem("Locate outputfile", 'o');
    setCtrlAccelerator(mi, 'O');
    mi.addActionListener(new OptionListener());
    gMenu.add(mi);
    //Chart
    mi = new JMenuItem("Graph", 'g');
    setCtrlAccelerator(mi, 'G');
    mi.addActionListener(new GraphListener());
    gMenu.add(mi);
    //plotposition
    mi = new JMenuItem("Plot position", 'e');
    setCtrlAccelerator(mi, 'E');
    mi.addActionListener(new position());
    gMenu.add(mi);
    return gMenu;
  }
 
 


  private void setCtrlAccelerator(JMenuItem mi, char acc)
  {
    KeyStroke ks = KeyStroke.getKeyStroke(
      acc, Event.CTRL_MASK
    );
    mi.setAccelerator(ks);
  }


  public void actionPerformed(ActionEvent event)
  {
    menuCmdLine = event.getActionCommand();
  }


  public void writeLog(String theString)
  {
    lWin.appendToChildWindow(theString);
  }


  void writeEcho(String theString)
  {
    cWin.appendToChildWindow(theString);
  }
}