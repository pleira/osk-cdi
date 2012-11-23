/*
 * EngineController.java
 *
 *  Model definition for a controller component provding 2 analog output ports.
 *  Control Function is time dependent and is currently hard coded inside model.
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
 *  2009-07-21
 *      File created  J. Eickhoff:
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A.Brandt
 *
 *
 *  2009-11
 *      OpenSimKit V 3.1.1
 *      Fixed bug in time handling.
 *      J. Eickhoff
 */
 package org.opensimkit.models.rocketpropulsion;

 import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.BaseModel;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.opensimkit.ports.AnalogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a controller component provding 2 analog output ports.
 * Control Function is time dependent and is currently hard coded inside model.
 *
 * @author J. Eickhoff
 * @version 1.2
 * @since 2.6.8
 */
public abstract class EngineController extends BaseModel {

	/** Logger instance for the EngineController. */
    private static final Logger LOG
            = LoggerFactory.getLogger(EngineController.class);
    /** Commandeable control value. */
     private double controlRangeMax;
     private double controlRangeMin;
     private double controlValue1Nom;
     private double controlValue2Nom;
     private double controlValueActual;
     private double controlValue1;
     private double controlValue2;
     private double localtime;

    private static final String TYPE      = "EngineController";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 10.0;
    private static final double MINTSTEP  = 0.001;
    private static final int    TIMESTEP  = 1;
    private static final int    REGULSTEP = 1;

     final AnalogPort controlPort1;
     final AnalogPort controlPort2;

    public EngineController(final String name, AnalogPort controlPort1, AnalogPort controlPort2) {
         super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
         this.controlPort1 = controlPort1;
         this.controlPort2 = controlPort2;
    }

    @Override
    @PostConstruct
    public void init() {
        /* Computation of derived initialization parameters. */
        localtime = 0.0;
        controlValueActual = controlRangeMax;
        controlValue1 = controlValueActual * controlValue1Nom;
        controlValue2 = controlValueActual * controlValue2Nom;
     	completeConnections();
    }
    
    void completeConnections() {
    	controlPort1.setToModel(this);
    	controlPort2.setToModel(this);
    	LOG.info("completeConnections for " + name + ", (" + controlPort1.getName()  + "," + controlPort2.getName() + ")" );
    }

    public double getControlRangeMax() {
		return controlRangeMax;
	}

	public void setControlRangeMax(double controlRangeMax) {
		this.controlRangeMax = controlRangeMax;
	}

	public double getControlRangeMin() {
		return controlRangeMin;
	}

	public void setControlRangeMin(double controlRangeMin) {
		this.controlRangeMin = controlRangeMin;
	}

    public double getControlValue1Nom() {
		return controlValue1Nom;
	}

	public void setControlValue1Nom(double controlValue1Nom) {
		this.controlValue1Nom = controlValue1Nom;
	}

	public double getControlValue2Nom() {
		return controlValue2Nom;
	}

	public void setControlValue2Nom(double controlValue2Nom) {
		this.controlValue2Nom = controlValue2Nom;
	}

    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.debug("% {} TimeStep-Computation", name);

        /**
         * Time dependent functionality of the controller has to be computed
         * here. For a PID controller e.g. the integrative and differential
         * part. For this simple controller signal reduction is computed keeping
         * relation of signals to each other.
         */
        if (localtime == 0.0) {
        localtime = localtime + tStepSize;
            return 0;
        }
        localtime = localtime + tStepSize;
        controlValueActual = controlValueActual - 0.001*tStepSize/0.5;
        if (controlValueActual < controlRangeMin) {
            controlValueActual = controlRangeMin;
        }
        controlValue1 = controlValueActual * controlValue1Nom;
        controlValue2 = controlValueActual * controlValue2Nom;
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
        LOG.debug("% {} BackiterStep-Computation", name);

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
        outFile.write("EngineController: '" + name + "'" + SimHeaders.NEWLINE);

        return 0;
    }


	//----------------------------------------
    // Methods added for JMX monitoring	

	@ManagedAttribute
	public double getControlValueActual() {
		return controlValueActual;
	}

	public void setControlValueActual(double controlValueActual) {
		this.controlValueActual = controlValueActual;
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
	public AnalogPort getControlPort1() {
		return controlPort1;
	}

	public AnalogPort getControlPort2() {
		return controlPort2;
	}
    
}
