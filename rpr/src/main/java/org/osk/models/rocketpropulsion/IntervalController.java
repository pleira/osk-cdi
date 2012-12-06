/*
 * IntervalController.java
 *
 *  Model definition for a controller component provding 2 analog output ports.
t this.name = name;  
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
 package org.osk.models.rocketpropulsion;

 import javax.annotation.PostConstruct;

import org.osk.interceptors.Log;
import org.osk.models.BaseModel;
import org.osk.ports.AnalogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for a controller component provding 2 analog output ports.
t this.name = name;  
 *  Control values for both channels are definable for up to 10 individual 
 *  intervals as value triples: onTime, offTime, onValue.
 *
 * @author J. Eickhoff
 */
@Log
public class IntervalController extends BaseModel {
    /** Logger instance for the IntervalController. */
    private static final Logger LOG
            = LoggerFactory.getLogger(IntervalController.class);
    /** Commandeable control value. */
     private double controlRangeMax;
     private double controlRangeMin;
     private double controlValue1;
     private double controlValue2;
     private double localtime;

    /**   Control settings. */
     private double ctrlSet0Chan1[] = new double[3];
     private double ctrlSet1Chan1[] = new double[3];
     private double ctrlSet2Chan1[] = new double[3];
     private double ctrlSet3Chan1[] = new double[3];
     private double ctrlSet4Chan1[] = new double[3];
     private double ctrlSet5Chan1[] = new double[3];
     private double ctrlSet6Chan1[] = new double[3];
     private double ctrlSet7Chan1[] = new double[3];
     private double ctrlSet8Chan1[] = new double[3];
     private double ctrlSet9Chan1[] = new double[3];

     private double ctrlSet0Chan2[] = new double[3];
     private double ctrlSet1Chan2[] = new double[3];
     private double ctrlSet2Chan2[] = new double[3];
     private double ctrlSet3Chan2[] = new double[3];
     private double ctrlSet4Chan2[] = new double[3];
     private double ctrlSet5Chan2[] = new double[3];
     private double ctrlSet6Chan2[] = new double[3];
     private double ctrlSet7Chan2[] = new double[3];
     private double ctrlSet8Chan2[] = new double[3];
     private double ctrlSet9Chan2[] = new double[3];

    private static final String TYPE      = "IntervalController";
    private static final String SOLVER    = "none";
    
    private static final int    REGULSTEP = 1;

     private AnalogPort controlPort1;
     private AnalogPort controlPort2;

    /**
     * Creates a new instance of the engine controller.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
     public IntervalController() {
        super(TYPE, SOLVER);
    }

    @PostConstruct
    public void init() {
        /* Computation of derived initialization parameters. */
        localtime = 0.0;
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

        LOG.info("time:  '{}' ", localtime);
        LOG.info("controlValue1:  '{}' ", controlValue1);
        LOG.info("controlValue2:  '{}' ", controlValue2);

        controlPort1.setAnalogValue(controlValue1);
        controlPort2.setAnalogValue(controlValue2);

        return 0;
    }

    public int regulStep() {
        LOG.info("controlValue1:  '{}' ", controlValue1);
        LOG.info("controlValue2:  '{}' ", controlValue2);

        controlPort1.setAnalogValue(controlValue1);
        controlPort2.setAnalogValue(controlValue2);

        return 0;
    }

    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@ManagedAttribute
	public double getControlRangeMax() {
		return controlRangeMax;
	}


	public void setControlRangeMax(double controlRangeMax) {
		this.controlRangeMax = controlRangeMax;
	}


	@ManagedAttribute
	public double getControlRangeMin() {
		return controlRangeMin;
	}


	public void setControlRangeMin(double controlRangeMin) {
		this.controlRangeMin = controlRangeMin;
	}


	@ManagedAttribute
	public double getControlValue1() {
		return controlValue1;
	}


	public void setControlValue1(double controlValue1) {
		this.controlValue1 = controlValue1;
	}


	@ManagedAttribute
	public double getControlValue2() {
		return controlValue2;
	}


	public void setControlValue2(double controlValue2) {
		this.controlValue2 = controlValue2;
	}


	@ManagedAttribute
	public double getLocaltime() {
		return localtime;
	}


	public void setLocaltime(double localtime) {
		this.localtime = localtime;
	}


	@ManagedAttribute
	public double[] getCtrlSet0Chan1() {
		return ctrlSet0Chan1;
	}


	public void setCtrlSet0Chan1(double[] ctrlSet0Chan1) {
		this.ctrlSet0Chan1 = ctrlSet0Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet1Chan1() {
		return ctrlSet1Chan1;
	}


	public void setCtrlSet1Chan1(double[] ctrlSet1Chan1) {
		this.ctrlSet1Chan1 = ctrlSet1Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet2Chan1() {
		return ctrlSet2Chan1;
	}


	public void setCtrlSet2Chan1(double[] ctrlSet2Chan1) {
		this.ctrlSet2Chan1 = ctrlSet2Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet3Chan1() {
		return ctrlSet3Chan1;
	}


	public void setCtrlSet3Chan1(double[] ctrlSet3Chan1) {
		this.ctrlSet3Chan1 = ctrlSet3Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet4Chan1() {
		return ctrlSet4Chan1;
	}


	public void setCtrlSet4Chan1(double[] ctrlSet4Chan1) {
		this.ctrlSet4Chan1 = ctrlSet4Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet5Chan1() {
		return ctrlSet5Chan1;
	}


	public void setCtrlSet5Chan1(double[] ctrlSet5Chan1) {
		this.ctrlSet5Chan1 = ctrlSet5Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet6Chan1() {
		return ctrlSet6Chan1;
	}


	public void setCtrlSet6Chan1(double[] ctrlSet6Chan1) {
		this.ctrlSet6Chan1 = ctrlSet6Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet7Chan1() {
		return ctrlSet7Chan1;
	}


	public void setCtrlSet7Chan1(double[] ctrlSet7Chan1) {
		this.ctrlSet7Chan1 = ctrlSet7Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet8Chan1() {
		return ctrlSet8Chan1;
	}


	public void setCtrlSet8Chan1(double[] ctrlSet8Chan1) {
		this.ctrlSet8Chan1 = ctrlSet8Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet9Chan1() {
		return ctrlSet9Chan1;
	}


	public void setCtrlSet9Chan1(double[] ctrlSet9Chan1) {
		this.ctrlSet9Chan1 = ctrlSet9Chan1;
	}


	@ManagedAttribute
	public double[] getCtrlSet0Chan2() {
		return ctrlSet0Chan2;
	}


	public void setCtrlSet0Chan2(double[] ctrlSet0Chan2) {
		this.ctrlSet0Chan2 = ctrlSet0Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet1Chan2() {
		return ctrlSet1Chan2;
	}


	public void setCtrlSet1Chan2(double[] ctrlSet1Chan2) {
		this.ctrlSet1Chan2 = ctrlSet1Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet2Chan2() {
		return ctrlSet2Chan2;
	}


	public void setCtrlSet2Chan2(double[] ctrlSet2Chan2) {
		this.ctrlSet2Chan2 = ctrlSet2Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet3Chan2() {
		return ctrlSet3Chan2;
	}


	public void setCtrlSet3Chan2(double[] ctrlSet3Chan2) {
		this.ctrlSet3Chan2 = ctrlSet3Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet4Chan2() {
		return ctrlSet4Chan2;
	}


	public void setCtrlSet4Chan2(double[] ctrlSet4Chan2) {
		this.ctrlSet4Chan2 = ctrlSet4Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet5Chan2() {
		return ctrlSet5Chan2;
	}


	public void setCtrlSet5Chan2(double[] ctrlSet5Chan2) {
		this.ctrlSet5Chan2 = ctrlSet5Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet6Chan2() {
		return ctrlSet6Chan2;
	}


	public void setCtrlSet6Chan2(double[] ctrlSet6Chan2) {
		this.ctrlSet6Chan2 = ctrlSet6Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet7Chan2() {
		return ctrlSet7Chan2;
	}


	public void setCtrlSet7Chan2(double[] ctrlSet7Chan2) {
		this.ctrlSet7Chan2 = ctrlSet7Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet8Chan2() {
		return ctrlSet8Chan2;
	}


	public void setCtrlSet8Chan2(double[] ctrlSet8Chan2) {
		this.ctrlSet8Chan2 = ctrlSet8Chan2;
	}


	@ManagedAttribute
	public double[] getCtrlSet9Chan2() {
		return ctrlSet9Chan2;
	}


	public void setCtrlSet9Chan2(double[] ctrlSet9Chan2) {
		this.ctrlSet9Chan2 = ctrlSet9Chan2;
	}


	@ManagedAttribute
	public AnalogPort getControlPort1() {
		return controlPort1;
	}


	public void setControlPort1(AnalogPort controlPort1) {
		this.controlPort1 = controlPort1;
	}


	@ManagedAttribute
	public AnalogPort getControlPort2() {
		return controlPort2;
	}


	public void setControlPort2(AnalogPort controlPort2) {
		this.controlPort2 = controlPort2;
	}
    
}
