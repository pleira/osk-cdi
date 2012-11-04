/*
 * IntervalController.java
 *
 *  Model definition for a controller component provding 2 analog output ports.
 *  Control values for both channels are definable for up to 10 individual 
 *  intervals as value triples: onTime, offTime, onValue.
 *
 *                 +-------------------------+
 *                 |                         |
 *                 |                         |+-- analog Control Port
 *                 |          ctrl           |
 *                 |                         |+-- analog Control Port
 *                 |                         |
 *                 +-------------------------+
 *
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-25-12
 *      File created  J. Eickhoff:
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
 package org.opensimkit.models.rocketpropulsion;

 import java.io.FileWriter;
 import java.io.IOException;
 import org.opensimkit.BaseModel;
 import org.opensimkit.Kernel;
 import org.opensimkit.manipulation.Manipulatable;
 import org.opensimkit.manipulation.Readable;
import org.opensimkit.ports.AnalogPort;
 import org.opensimkit.SimHeaders;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a controller component provding 2 analog output ports.
 *  Control values for both channels are definable for up to 10 individual 
 *  intervals as value triples: onTime, offTime, onValue.
 *
 * @author J. Eickhoff
 * @version 1.0
 * @since 3.3
 */
public class IntervalController extends BaseModel {
    /** Logger instance for the IntervalController. */
    private static final Logger LOG
            = LoggerFactory.getLogger(IntervalController.class);
    /** Commandeable control value. */
    @Manipulatable private double controlRangeMax;
    @Manipulatable private double controlRangeMin;
    @Readable private double controlValue1;
    @Readable private double controlValue2;
    @Readable private double localtime;

    /**   Control settings. */
    @Manipulatable private double ctrlSet0Chan1[] = new double[3];
    @Manipulatable private double ctrlSet1Chan1[] = new double[3];
    @Manipulatable private double ctrlSet2Chan1[] = new double[3];
    @Manipulatable private double ctrlSet3Chan1[] = new double[3];
    @Manipulatable private double ctrlSet4Chan1[] = new double[3];
    @Manipulatable private double ctrlSet5Chan1[] = new double[3];
    @Manipulatable private double ctrlSet6Chan1[] = new double[3];
    @Manipulatable private double ctrlSet7Chan1[] = new double[3];
    @Manipulatable private double ctrlSet8Chan1[] = new double[3];
    @Manipulatable private double ctrlSet9Chan1[] = new double[3];

    @Manipulatable private double ctrlSet0Chan2[] = new double[3];
    @Manipulatable private double ctrlSet1Chan2[] = new double[3];
    @Manipulatable private double ctrlSet2Chan2[] = new double[3];
    @Manipulatable private double ctrlSet3Chan2[] = new double[3];
    @Manipulatable private double ctrlSet4Chan2[] = new double[3];
    @Manipulatable private double ctrlSet5Chan2[] = new double[3];
    @Manipulatable private double ctrlSet6Chan2[] = new double[3];
    @Manipulatable private double ctrlSet7Chan2[] = new double[3];
    @Manipulatable private double ctrlSet8Chan2[] = new double[3];
    @Manipulatable private double ctrlSet9Chan2[] = new double[3];

    private static final String TYPE      = "IntervalController";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 10.0;
    private static final double MINTSTEP  = 0.001;
    private static final int    TIMESTEP  = 1;
    private static final int    REGULSTEP = 1;

    @Manipulatable private AnalogPort controlPort1;
    @Manipulatable private AnalogPort controlPort2;

    /**
     * Creates a new instance of the engine controller.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
     public IntervalController(final String name, final Kernel kernel) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }


    /**
    * The initialization of the Component takes place in this method. It is
    * called after the creation of the instance and the loading of its default
    * values so that derived variables can be calculated after loading or
    * re-calculated after the change of a manipulatable variable (but in this
    * case the init method must be called manually!).
    */
    @Override
    public void init() {
        /* Computation of derived initialization parameters. */
        localtime = 0.0;
        LOG.debug("% {} Init", name);
        System.out.println("Time " + localtime);
        System.out.println("Init " + ctrlSet0Chan1[2]);
        System.out.println("Init " + ctrlSet0Chan2[2]);
        controlValue1 = ctrlSet0Chan1[2];
        controlValue2 = ctrlSet0Chan2[2];
        System.out.println("TheValues" + ctrlSet0Chan1[0] + " " +  ctrlSet0Chan1[1] + " " + ctrlSet0Chan1[2]);
        System.out.println("TheValues" + ctrlSet1Chan1[0] + " " +  ctrlSet1Chan1[1] + " " + ctrlSet1Chan1[2]);
        System.out.println("TheValues" + ctrlSet2Chan1[0] + " " +  ctrlSet2Chan1[1] + " " + ctrlSet2Chan1[2]);
        System.out.println("TheValues" + ctrlSet3Chan1[0] + " " +  ctrlSet3Chan1[1] + " " + ctrlSet3Chan1[2]);
        System.out.println("TheValues" + ctrlSet4Chan1[0] + " " +  ctrlSet4Chan1[1] + " " + ctrlSet4Chan1[2]);
        System.out.println("TheValues" + ctrlSet5Chan1[0] + " " +  ctrlSet5Chan1[1] + " " + ctrlSet5Chan1[2]);
        System.out.println("TheValues" + ctrlSet6Chan1[0] + " " +  ctrlSet6Chan1[1] + " " + ctrlSet6Chan1[2]);
        System.out.println("TheValues" + ctrlSet7Chan1[0] + " " +  ctrlSet7Chan1[1] + " " + ctrlSet7Chan1[2]);
        System.out.println("TheValues" + ctrlSet8Chan1[0] + " " +  ctrlSet8Chan1[1] + " " + ctrlSet8Chan1[2]);
        System.out.println("TheValues" + ctrlSet9Chan1[0] + " " +  ctrlSet9Chan1[1] + " " + ctrlSet9Chan1[2]);
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {

        if (localtime == 0.0) {
            localtime = localtime + tStepSize;
            return 0;
        }

        localtime = localtime + tStepSize;
        /**
         * Interval dependent control value setting for channel 1.
         */
        if (localtime >= ctrlSet0Chan1[0]) {
            controlValue1 = ctrlSet0Chan1[2];
        }
        if (localtime >= ctrlSet1Chan1[0]) {
            controlValue1 = ctrlSet1Chan1[2];
        }
        if (localtime >= ctrlSet2Chan1[0]) {
            controlValue1 = ctrlSet2Chan1[2];
        }
        if (localtime >= ctrlSet3Chan1[0]) {
            controlValue1 = ctrlSet3Chan1[2];
        }
        if (localtime >= ctrlSet4Chan1[0]) {
            controlValue1 = ctrlSet4Chan1[2];
        }
        if (localtime >= ctrlSet5Chan1[0]) {
            controlValue1 = ctrlSet5Chan1[2];
        }
        if (localtime >= ctrlSet6Chan1[0]) {
            controlValue1 = ctrlSet6Chan1[2];
        }
        if (localtime >= ctrlSet7Chan1[0]) {
            controlValue1 = ctrlSet7Chan1[2];
        }
        if (localtime >= ctrlSet8Chan1[0]) {
            controlValue1 = ctrlSet8Chan1[2];
        }
        if (localtime >= ctrlSet9Chan1[0]) {
            controlValue1 = ctrlSet9Chan1[2];
        }
        if (localtime >= ctrlSet9Chan1[1]) {
            controlValue1 = 0.0;
        }


        /**
         * Interval dependent control value setting for channel 2.
        */
        if (localtime >= ctrlSet0Chan2[0]) {
            controlValue2 = ctrlSet0Chan2[2];
        }
        if (localtime >= ctrlSet1Chan2[0]) {
            controlValue2 = ctrlSet1Chan2[2];
        }
        if (localtime >= ctrlSet2Chan2[0]) {
            controlValue2 = ctrlSet2Chan2[2];
        }
        if (localtime >= ctrlSet3Chan2[0]) {
            controlValue2 = ctrlSet3Chan2[2];
        }
        if (localtime >= ctrlSet4Chan2[0]) {
            controlValue2 = ctrlSet4Chan2[2];
        }
        if (localtime >= ctrlSet5Chan2[0]) {
            controlValue2 = ctrlSet5Chan2[2];
        }
        if (localtime >= ctrlSet6Chan2[0]) {
            controlValue2 = ctrlSet6Chan2[2];
        }
        if (localtime >= ctrlSet7Chan2[0]) {
            controlValue2 = ctrlSet7Chan2[2];
        }
        if (localtime >= ctrlSet8Chan2[0]) {
            controlValue2 = ctrlSet8Chan2[2];
        }
        if (localtime >= ctrlSet9Chan2[0]) {
            controlValue2 = ctrlSet9Chan2[2];
        }
        if (localtime >= ctrlSet9Chan2[1]) {
            controlValue2 = 0.0;
        }

        // Crosscheck to avoid values being set out of bounds
        if (controlValue1 < controlRangeMin) {
            controlValue1 = controlRangeMin;
        }
        if (controlValue2 < controlRangeMin) {
            controlValue2 = controlRangeMin;
        }

        if (controlValue1 > controlRangeMax) {
            controlValue1 = controlRangeMax;
        }
        if (controlValue2 > controlRangeMax) {
            controlValue2 = controlRangeMax;
        }

        LOG.debug("% {} TimeStep-Computation", name);
        LOG.debug("time:  '{}' ", localtime);
        LOG.debug("controlValue1:  '{}' ", controlValue1);
        LOG.debug("controlValue2:  '{}' ", controlValue2);

        controlPort1.setAnalogValue(controlValue1);
        controlPort2.setAnalogValue(controlValue2);

        return 0;
    }


    @Override
    public int iterationStep() {
        LOG.debug("% {} IterationStep-Computation", name);

        return 0;
    }


    @Override
    public int backIterStep() {
/*        LOG.debug("% {} BackiterStep-Computation", name);
        LOG.debug("controlValue1:  '{}' ", controlValue1);
        LOG.debug("controlValue2:  '{}' ", controlValue2);

        System.out.println("BackiterStep" + controlValue1);
        System.out.println("BackiterStep" + controlValue2);
        controlPort1.setAnalogValue(controlValue1);
        controlPort2.setAnalogValue(controlValue2);

*/
                return 0;
    }


    @Override
    public int regulStep() {
        LOG.debug("% {} RegulStep-Computation", name);
        LOG.debug("controlValue1:  '{}' ", controlValue1);
        LOG.debug("controlValue2:  '{}' ", controlValue2);

        controlPort1.setAnalogValue(controlValue1);
        controlPort2.setAnalogValue(controlValue2);

        return 0;
    }


    @Override
    public int save(final FileWriter outFile) throws IOException {
        outFile.write("IntervalController: '" + name + "'" + SimHeaders.NEWLINE);

        return 0;
    }
}
