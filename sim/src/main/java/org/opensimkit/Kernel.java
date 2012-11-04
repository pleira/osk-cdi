/*
 * Kernel.java
 *
 * Created on 3. Juli 2007, 18:40
 *
 * Kernel object of the OpenSimKit simulation architecture. This kernel object
 * controls the overall simulation functionality.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
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
 *      No warranty and liability for correctness by author.
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2006-03
 *      OpenSimKit V 2.3
 *      Modifications entered for I/O file handling via cmd line arguments by
 *      J. Eickhoff
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *
 *  2011-01
 *      Provider-Subscriber Prototype implemented by
 *      A. Brandt
 *      This mechanism is used for data interchange between models which have no
 *      physical line connection (data line, power line, fluid pipe etc.)
 *
 *
 *  2011-01
 *      Applied bugfix:
 *      The providerSubscriber instance needs to be created before the SeqModSim 
 *      instance.
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
 */

package org.opensimkit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Manipulator;
import org.opensimkit.providerSubscriber.ProviderSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kernel object of the OpenSimKit simulation architecture. This kernel object
 * controls the overall simulation functionality.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author T. Pieper
 * @version 2.0
 * @since 2.4.0
 */
@ApplicationScoped
public class Kernel {
    public static final String OSK_NAME = "OpenSimKit Java";
    public static final String OSK_VERSION = "B4.0.0";
    private static final Logger LOG = LoggerFactory.getLogger(Kernel.class);

    @Inject Manipulator  manipulator;

    @Inject TimeHandler  timeHandler;
        
    @Inject TabGenerator tabGenerator;
    @Inject ProviderSubscriber providerSubscriber;

    private final String name  = "SimKernel";
    
    @Inject ComHandler   compColl;
    @Inject PortHandler  portColl;
    @Inject MeshHandler  meshColl;
    @Inject SeqModSim    simul;
//    @Inject Instance<StaxInput> staxInput;

    private       FileWriter   outTabStream;
    private       FileWriter   outData;
    private       int          localNAckFlag;
    /** Specifying successful opening input file. */
    private       int          inputOpenFlag;
    /** Specifying successful opening tabGenerator table file. */
    private       int          tabOpenFlag;
    /** Specifying successful loading of a model. */
    private       int          loadFlag;
    /** Specifying, that at least part of a model has been read. If an error
     *  occurs, no simple rereading of another file is possible, as some objects
     *  already where created. */
    private       int          inputReadFlag;
    /** Specifying successful finishing of a computation run. */
    private       int          compFlag;
    private       int          resumeFlag;
    /** Needed to preserve the state of the simulation. */
    private       boolean      isSimulationRunning;
    private       OutputStream outputStream;
    @Manipulatable private String  systemDescription;
    @Manipulatable private String  caseDescription;
    @Manipulatable private String  noteDescription;
    @Manipulatable private String  numMethod; //Not used
    @Manipulatable private boolean debug;
    @Manipulatable private double  relAccuracy;
    @Manipulatable private double  absAccuracy;
	
    @Inject
    public Kernel()  {
    	LOG.info(name);
        LOG.debug(SimHeaders.DEBUG_SHORT, "Constructor {}", name);
    }

    @PostConstruct
    public void init() {
        LOG.debug("Initialization");
        manipulator.registerInstance(getName(), this);
        manipulator.registerInstance(timeHandler.getName(), timeHandler);
        // reading in the input file.
        localNAckFlag = 0;
        inputOpenFlag = 0;
        inputReadFlag = 0;
        loadFlag = 0;
        compFlag = 0;
        SimHeaders.newDebugFlag = debug;
        SimHeaders.epsrel = relAccuracy;
        SimHeaders.epsabs = absAccuracy;
        SimHeaders.negativeAckFlag = 0;
        LOG.info("System initialization successful. No model loaded yet.");
        return;
    }

//    public void initSim(@Observes ContainerInitialized init)  {
//    LOG.info("built: " +  this.getName());	  
//    }
    
    public int openInput(final String fileName) {
        LOG.debug("Opening input file...");

        // opening the input file.
        if (loadFlag == 1) {
            // File already open
            return 0;
        } else if (inputReadFlag == 1 && loadFlag == 0) {
            LOG.error(
                "Load command refused. Parts of another model already loaded.");
            return 1;
        }
        inputOpenFlag = 1;
        return 0;
    }

// Open ouput table file and init TabGenerator
//-----------------------------------------------------------------------------
    public int openOutput(final String outTabName)
        throws IOException {

        LOG.debug(SimHeaders.DEBUG_SHORT, "Opening output file...");

        if (inputOpenFlag == 0) {
            return 1;
        }
        // opening the tabGenerator table file.
        outTabStream = new FileWriter(outTabName, true);
        if (tabGenerator.init(outTabStream) == 1) {
            // Outfiles not properly opened
            return 1;
        }
        tabOpenFlag = 1;
        return 0;
    }

    public int load() throws IOException {

        if (inputOpenFlag == 0 || tabOpenFlag == 0) {
            // In- and outfiles not properly opened
            return 1;
        }
        if (loadFlag == 1) {
            // File already loaded
            return 0;
        }
        localNAckFlag = 0;
        SimHeaders.newDebugFlag = false;
        SimHeaders.epsrel = 0.05;
        SimHeaders.epsabs = 0.05;

        LOG.debug(SimHeaders.DEBUG_SHORT, "Loading input file...");

        // Loading of kernel parameters
        //---------------------------------------------------------------------
        LOG.debug(SimHeaders.DEBUG_SHORT, "Loading data of system kernel...");

        inputReadFlag = 1;
        // Specifying, that at least part of a model has been read.
        // If an error occurs, no simple rereading of another file
        // is possible, as objects already where created.


        // Opening the log file where all models have access to. The
        // File is closed by the Kernel's destructor.
        if (SimHeaders.newDebugFlag == true) {
            SimHeaders.logFile = new FileWriter("Simulation.log", true);
        }

        /** It is important to honor the order in which the different parts
         *  of the simulator are loaded and initialised!
         * System
         * Models
         * Meshes (First loading, then initialisation)
         * Ports
         * Netlist
         * providerSubscriberTable
         * simul.load(xmlinput)
         * simul.calcStepInit(compColl, meshColl, tabGenerator)
         * tabGenerator.load(xmlinput, compColl)
         */

        /** Stax input reader obtained by CDI container. */
//        StaxInput stax = staxInput.get();
//        try {
//            // Create the XML event reader
//            FileReader reader = new FileReader(SimHeaders.myInFileName);
//
//            stax.process(reader);
//        } catch (FileNotFoundException ex) {
//            LOG.error("Exception:", ex);
//        } catch (XMLStreamException ex) {
//            LOG.error("Exception:", ex);
//        }

        LOG.debug(SimHeaders.DEBUG_SHORT, "PortCollection sucessfully loaded.");
        LOG.debug(SimHeaders.DEBUG_SHORT, "Ports sucessfully initialized.");

        //
        // Loading the Simulation-Data
        //--------------------------
        LOG.debug(SimHeaders.DEBUG_SHORT,
                "Simulation run data sucessfully loaded.");

        // init the Calc.-Steps Time- and Regul
        //--------------------------
        if (simul.calcStepInit(compColl, meshColl, tabGenerator) == 1) {
            return 1;
        }
        LOG.debug(SimHeaders.DEBUG_SHORT,
                "Calculation Steps sucessfully initialized.");

        //
        // Loading the Table Output Generator Data
        //--------------------------
        LOG.debug(SimHeaders.DEBUG_SHORT,
                "Table Generator sucessfully initialized.");

        if (localNAckFlag > 0) {
            SimHeaders.negativeAckFlag = 1;
        }
        if (SimHeaders.negativeAckFlag > 0) {
            LOG.info("Model loading not successfully finished.");
            return 1;
        }
        loadFlag = 1;
        LOG.info("Model sucessfully loaded.");
        return 0;
    }

    public int compute() throws IOException {
        LOG.debug(SimHeaders.DEBUG_SHORT, "Compute");

        if (compFlag > 0) {
            return 0;
        }
        if (loadFlag > 0) {
            if (simul.compute() == 1) {
                return 1;
            }
        } else {
            LOG.error("No model successfully loaded yet!");
            return 1;
        }

        compFlag = 1;
        return 0;
    }

    // Kernel parameters to be accessed by other objects
    //-------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    public String getSysDescr() {
        return systemDescription;
    }

    public String getCaseDescr() {
        return caseDescription;
    }

    public String getNoteDescr() {
        return noteDescription;
    }

    public int getInputReadFlag() {
        return inputReadFlag;
    }

    // FOR DEBUGGING
    //-------------------------------------------------------------------------
    public void showConnection() {
        portColl.showLinks();
    }

    // MISCELLANEOUS
    //-------------------------------------------------------------------------
    public int save() throws IOException {
        LOG.debug(SimHeaders.DEBUG_SHORT, "Save");

        FileWriter outData = new FileWriter("Output.dat", true);
        if (compColl.save(outData) == 1) {
            return 1;
        }
        return 0;
    }

    public void setTabOpenFlag(final int value) {
        tabOpenFlag = value;
    }

    public synchronized boolean getIsSimulationRunning() {
        return isSimulationRunning;
    }

    public synchronized void setIsSimulationRunning(
        final boolean isSimulationRunning) {

        this.isSimulationRunning = isSimulationRunning;
        simul.setIsComputing(true);
    }

    public synchronized void pauseSimulation() {
        simul.setIsComputing(false);
        simul.setStateToPaused();
    }

    public synchronized void stopSimulation() {
        simul.setIsComputing(false);
        simul.setStateToStopping();
    }

    public synchronized void resumeSimulation() {
        simul.setIsComputing(true);
        simul.setStateToRunning();
    }

    public synchronized SimulatorState getState() {
        return simul.getState();
    }

    public void start() {
    }

    public void setOutputWriter(final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public OutputStream getOutputWriter() {
        return outputStream;
    }

    public ComHandler getComHandler() {
        return compColl;
    }

    public MeshHandler getMeshHandler() {
        return meshColl;
    }

    public PortHandler getPortHandler() {
        return portColl;
    }

    public TabGenerator getTabGenerator() {
        return tabGenerator;
    }

    public SeqModSim getSeqModSim() {
        return simul;
    }

    public TimeHandler getTimeHandler() {
        return timeHandler;
    }

    public Manipulator getManipulator() {
        return manipulator;
    }
    
    public ProviderSubscriber getProviderSubscriber() {
        return providerSubscriber;
    }
}
