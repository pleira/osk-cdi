/*
 * InteractiveMain.java
 *
 * Created on 3. Juli 2007, 18:15
 *
 * Main entry point of OpenSimKit program.
 *
 * ----------------------------------------------------------------------------
 * 2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by autor.
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2006-03
 *      OpenSimKit V 2.3
 *      Modifications entered for cleaner handling of globals.
 *      Modifications enterd for I/O file handling via cmd line arguments.
 *      Modifications enterd for error handling via exceptions.
 *      J. Eickhoff
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2008-07
 *      OpenSimKit V 2.4.6
 *      Modifications for interactive simulation runs.
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *
 *  2010-02
 *     Integrated SimVisThread handling for connection to 3d visualization.
 *     J. Eickhoff
 *
 *
 *  2010-03
 *     Moved SimVisThread handling to ScStructure class. This allows individual
 *     threads for each simulated flying object.
 *     J. Eickhoff
 *
 *
 *  2010-04
 *     Cleaned up to assure proper socket closing when terminating simulator.
 *     J. Eickhoff
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *-----------------------------------------------------------------------------
 */

package org.opensimkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main entry point of OpenSimKit program.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 2.2
 * @since 2.4.6
 */
@ApplicationScoped
public class InteractiveMain {
    private static Logger LOG = LoggerFactory.getLogger(InteractiveMain.class);
    private static SimCmdThread cmdThread;
    static ComputeThread sCompThread;
    @Inject ComputeThread compThread;
 
    @Inject ParametersFactory pF;
     
    @Inject Kernel kernel;
    
    @Inject Instance<SimVisThread> visThreadInstance;
    
    static ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * entry point of the OpenSimKit simulator. From here the loading of
     * the input file is triggered.
     * @param args ContainerInitialized init
     * @throws java.io.IOException
     */
    public void initSim(@Observes ContainerInitialized init) throws IOException {
//    public void main(final String[] args) throws IOException {
        int cmdShutFlag;
        int compShutFlag;
        String[] foundLibraries;
        // static field was not initialized by CDI
        sCompThread = compThread;
        cmdShutFlag = 0;
        compShutFlag = 0;
        
        //List<String> args = pF.getArgs(); // argsValidator.getValidParameters();
//        if (args.size() < 2) {
//           LOG.error("Pass 2 arguments: inputfile.xml outfile");
//           exit(1);
//        }
        
        //LOG.info(a.hi());

        LOG.info(greet());
        LOG.info(getComputerInformation());

//        SimHeaders.myInFileName = args.get(0);
//        SimHeaders.myOutFileName = args.get(1);
//        LOG.info("Input File: {}", SimHeaders.myInFileName);
//        LOG.info("Output File: {}", SimHeaders.myOutFileName);

        Socket tcSocket, tmSocket;
        try {
            LOG.info("Simulator started...");
            // With CDI, the kernel should be ready here
//            kernel.openInput(SimHeaders.myInFileName);
//            kernel.openOutput(SimHeaders.myOutFileName);
//            kernel.load();
//            define meshes            
//            LOG.info("Waiting for Cmd/Ctrl connection on port 1500...");
//            ServerSocket telecommandSocket = new ServerSocket(1500);
//            telecommandSocket.setSoTimeout(1000);
//            try {
//            tcSocket = telecommandSocket.accept();
//            } catch (SocketTimeoutException e) {
//            	// nothing
//            }
//            LOG.info("Waiting for output console connection on port 1510...");
//            ServerSocket telemetrySocket = new ServerSocket(1510);
//            telemetrySocket.setSoTimeout(1000);
//            try {
//            	// This socket should be injected, having different alternatives
//            tmSocket = telemetrySocket.accept();
//            kernel.setOutputWriter(tmSocket.getOutputStream());
//            } catch (SocketTimeoutException e) {
//                LOG.warn("no tm socket available...");
//            } catch (IOException e) {
//                LOG.warn("no io to tm socket...");
//
//            }            
            
//            LOG.info("Generating computation thread...");
//            compThread = new ComputeThread();
            
//            LOG.debug("getName {}", compThread.getName());

            //LOG.info("Generating command thread...");
            //cmdThread = new SimCmdThread(compThread, tcSocket, kernel);
            //cmdThread.start();


//            LOG.info("Generating visualization thread...");
//            SimVisThread visThread = visThreadInstance.get();
//            visThread.connectToCelestia();
            // normally, a user command should start the simulation, but for now...
            OutputStream writer = new FileOutputStream("simulation.log");
            kernel.setOutputWriter(writer);
//            startSimulation();
            sCompThread.run();
            
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    LOG.error("Exception:", e);
                }
                // shutFlag = cmdThread.getstatus();
                cmdShutFlag = cmdThread.getstatus();
                //compShutFlag = compThread.isterminated();
                if (cmdShutFlag > 0) {
                    break;
                }
                if (compShutFlag > 0) {
                    break;
                }
            }
            if (cmdShutFlag == 2 || compShutFlag == 2) {
//                LOG.info("System terminating due broken connections...");
//                try {
//                    tcSocket.close();
//                }
//                catch (IOException e2) {
//                    }
//                try {
//                    tmSocket.close();
//                }
//                catch (IOException e2) {
//                }
                exit(1);
            }
            if (cmdShutFlag == 1 || compShutFlag == 1) {
                LOG.info("System terminated");
//                try {
//                    tcSocket.close();
//                }
//                catch (IOException e2) {
//                    }
//                try {
//                    tmSocket.close();
//                }
//                catch (IOException e2) {
//                }
                exit(0);
            }
        } catch (IOException e) {
            LOG.error("Exception:", e);
//            try {
//                tcSocket.close();
//            }
//            catch (IOException e2) {
//            }
//            try {
//                tmSocket.close();
//            }
//            catch (IOException e2) {
//            }
            exit(1);
        }
        //
        // Normal system termination
//        try {
//            tcSocket.close();
//        }
//        catch (IOException e2) {
//        }
//        try {
//            tmSocket.close();
//        }
//        catch (IOException e2) {
//        }
    }


//
//    /**
//     * Adds all libraries (jar files) found inside path to the classpath.
//     * @param path Path from which all jar files shall be added to the
//     * classpath.
//     * @return All jar files found in path.
//     * @throws java.io.IOException
//     */
//    private static String[] addLibrariesToClasspath(final String path)
//        throws IOException {
//        final Class[] parameters = new Class[]{URL.class};
//        URLClassLoader sysLoader;
//        String[] result = null;
//
//        sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//
//        File[] fileList;
//        URL[] urls;
//        File dir = new File(path);
//
//        FilenameFilter filter = new FilenameFilter() {
//
//            public boolean accept(final File dir, final String name) {
//                return name.endsWith(".jar");
//            }
//        };
//
//        fileList = dir.listFiles(filter);
//        Arrays.sort(fileList);
//
//        if (fileList != null) {
//
//            urls = new URL[fileList.length];
//            result = new String[fileList.length];
//
//            for (int i = 0; i < fileList.length; i++) {
//                urls[i] = fileList[i].toURI().toURL();
//                result[i] = fileList[i].getCanonicalPath();
//            }
//
//            for (int i = 0; i < urls.length; i++) {
//                Class<?> sysclass = URLClassLoader.class;
//
//                try {
//                    Method method =
//                            sysclass.getDeclaredMethod("addURL", parameters);
//                    method.setAccessible(true);
//                    method.invoke(sysLoader, new Object[]{urls[i]});
//                } catch (Exception t) {
//                    t.printStackTrace();
//                    throw new IOException("Error, could not add URL to"
//                            + " system classloader");
//                }
//            }
//        }
//        return result;
//    }



    /**
     * This method shall be the only one inside the simulator which calls
     * System.exit(). In every other place this method shall be called instead.
     * @param status
     */
    public static void exit(final int status) {
        System.exit(status);
    }


    public static synchronized void startSimulation() {
        executor.submit(sCompThread);
    }


    public static synchronized void shutdownSimulation() {
        executor.shutdownNow();
    }


    public static String getComputerInformation() {
        StringBuilder result = new StringBuilder();

        result.append("Computer Information:");
        result.append(SimHeaders.NEWLINE);
        result.append("Java: ");
        result.append(System.getProperty("java.version"));
        result.append("; ");
        result.append(System.getProperty("java.vm.name"));
        result.append(" ");
        result.append(System.getProperty("java.vm.version"));
        result.append(SimHeaders.NEWLINE);

        result.append("System: ");
        result.append(System.getProperty("os.name"));
        result.append(" (");
        result.append(System.getProperty("os.version"));
        result.append(") ");
        result.append(System.getProperty("sun.os.patch.level"));
        result.append(" running on ");
        result.append(System.getProperty("os.arch"));
        result.append(" (");
        result.append(System.getProperty("sun.arch.data.model"));
        result.append(" bit)");
        result.append("; ");
        result.append(System.getProperty("file.encoding"));
        result.append("; ");
        result.append(System.getProperty("user.language"));
        result.append("_");
        result.append(System.getProperty("user.country"));
        result.append(SimHeaders.NEWLINE);

        result.append("Timezone: ");
        result.append(System.getProperty("user.timezone"));
        result.append(SimHeaders.NEWLINE);

        result.append("Userdir: ");
        result.append(System.getProperty("user.dir"));
        result.append(SimHeaders.NEWLINE);

        return result.toString();
    }


    public static String greet() {
        StringBuilder result = new StringBuilder();

        result.append(Kernel.OSK_NAME);
        result.append(" ");
        result.append(Kernel.OSK_VERSION);
        result.append(SimHeaders.NEWLINE);
        result.append("-------------------------------");
        result.append(SimHeaders.NEWLINE);

        return result.toString();
    }
}
