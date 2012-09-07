/*
* OSKConsole.java
*
*  This class connects the MMI to the simulator
*
*-----------------------------------------------------------------------------
* Modification History:
*
*  2008-12-31
*      File written by Jens Eickhoff
*      File under GPL see OpenSimKit Documentation.
*      No warranty and liability for correctness by author.
*
*/

package org.opensimkit.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;

/**
 *
 * @author J. Eickhoff
 */
public class OSKConsole {
    private static final String LIB_PATH = "../lib/";

    /**
     * OSK Console class & main function.
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        ConsoleGUI frame = new ConsoleGUI();
        frame.setLocation(100, 100);
        frame.setSize(500, 400);
        frame.setVisible(true);
        String consoleCmdline;
        String menuCmdline = "";
        String line;
        String[] foundLibraries;

        Socket cmdSock = null;
        Socket simResultSock = null;

        if (args.length != 1) {
            System.err.println("Usage: java -jar osk-j-mmi.jar <host>");
            System.exit(1);
        }

        foundLibraries = addLibrariesToClasspath(LIB_PATH);
        if (foundLibraries != null) {
            System.out.println("The following 3rd party libraries were found:");
            for (String library : foundLibraries) {
                System.out.println(library);
            }
        }

        try {
//      cmdSocket cmdSock = new cmdSocket(args[0], 7);
            try {
                cmdSock = new Socket("localhost", 1500);
                System.out.println("Connected to server " +
                        cmdSock.getInetAddress() +
                        ":" + cmdSock.getPort());
            } catch (UnknownHostException e) {
                System.out.println(e);
                System.exit(1);
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }

            try {
                simResultSock = new Socket("localhost", 1510);
                System.out.println("Connected to server " +
                        simResultSock.getInetAddress() +
                        ":" + simResultSock.getPort());
            } catch (UnknownHostException e) {
                System.out.println(e);
                System.exit(1);
            }

            OutputStream cmdStream = cmdSock.getOutputStream();
            InputStream cmdEchoStream = cmdSock.getInputStream();
            InputStream simResultStream = simResultSock.getInputStream();

            //Define timeout to avoid locking program with socket write not being served by simulator
            cmdSock.setSoTimeout(300);

            //Generate console viewer thread for command echos
            ViewerThread vth = new ViewerThread(cmdEchoStream, frame);
            vth.start();

//            //Generate console viewer thread for simulator results
//            LoggerThread lth = new LoggerThread(simResultSock.getChannel(),
//                    simResultStream, frame);
//            lth.start();
//            try {
//    			Thread.sleep(500);
//    		} catch (InterruptedException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		}

            BufferedReader conin = new BufferedReader(
                    new InputStreamReader(System.in));


            WinGrabberThread cWinGrabber = new WinGrabberThread(frame.getCWin());
            cWinGrabber.start();


            // This is the commander thread, reading user commands and sending via "out" to SimDummy
            while (true) {
                // All steps for reading cmd line from keyboard disabled -> GUI only
                System.out.println("Resetting strings------------------ ");
                line = "";
                menuCmdline = "";
                consoleCmdline = "";

                // Reading the command from the GUI
                consoleCmdline = cWinGrabber.readcWinLine();
                cWinGrabber.resetcWinLine();
                menuCmdline = frame.readMenuLine();
                frame.resetMenuLine();

                if (menuCmdline != "") {
                    line = menuCmdline;
                } else if (consoleCmdline != "") {
                    line = consoleCmdline;
                }

                // Processing the command
                System.out.println("Console submitted command: " + line);
                if (line != "") {
                    // Send line to SimDummy
                    cmdStream.write(line.getBytes());
                    cmdStream.write('\r');
                    cmdStream.write('\n');
                }
                // Pause reader streams shortly to grab reply (for non premptive multitasking kernels)
                ViewerThread.yield();
//                LoggerThread.yield();
//                try {
//        			Thread.sleep(500);
//        		} catch (InterruptedException e) {
//        			// TODO Auto-generated catch block
//        			e.printStackTrace();
//        		}
                WinGrabberThread.yield();
                if (line.equalsIgnoreCase("disconnect")) {
                    break;
                }
                if (line.equalsIgnoreCase("shutdown")) {
                    break;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
            // Terminating program
            //System.out.println("Terminating cWinGrabber thread...");
            cWinGrabber.interrupt();
            cWinGrabber.requestStop();
            WinGrabberThread.yield();
            //System.out.println("Terminating cmd/ctrl thread...");
            vth.requestStop();
            ViewerThread.yield();
            //System.out.println("Terminating simulation result logger thread...");
//            lth.requestStop();
//            LoggerThread.yield();
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//            }
            frame.setVisible(false);
            frame.dispose();
            cmdEchoStream.close();
            cmdStream.close();
            cmdSock.close();
            System.out.println("Closed Cmd Socket...");
            simResultStream.close();
            simResultSock.close();
            System.out.println("Closed Simulator Result Socket...");
        } catch (IOException e) {
            System.err.println(e.toString());
            frame.setVisible(false);
            frame.dispose();
            System.exit(1);
        }
        System.out.println("Bye...");
        System.exit(0);
    }

    /**
     * Adds all libraries (jar files) found inside path to the classpath.
     * @param path Path from which all jar files shall be added to the
     * classpath.
     * @return All jar files found in path.
     * @throws java.io.IOException
     */
    private static String[] addLibrariesToClasspath(final String path)
            throws IOException {
        final Class[] parameters = new Class[]{URL.class};
        URLClassLoader sysLoader;
        String[] result = null;

        sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        File[] fileList;
        URL[] urls;
        File dir = new File(path);

        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        };

        fileList = dir.listFiles(filter);

        if (fileList != null) {

            urls = new URL[fileList.length];
            result = new String[fileList.length];

            for (int i = 0; i < fileList.length; i++) {
                urls[i] = fileList[i].toURI().toURL();
                result[i] = fileList[i].getCanonicalPath();
            }

            for (int i = 0; i < urls.length; i++) {
                Class<?> sysclass = URLClassLoader.class;

                try {
                    Method method = sysclass.getDeclaredMethod("addURL", parameters);
                    method.setAccessible(true);
                    method.invoke(sysLoader, new Object[]{urls[i]});
                } catch (Exception t) {
                    t.printStackTrace();
                    throw new IOException("Error, could not add URL to" + " system classloader");
                }
            }
        }

        return result;
    }
}

