/*
 * EngineController.java
 *
 *  Model definition for a controller component providing 2 analog output ports.
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
 *      File under GPL  see OpenSimKit Documentation.
 */
package org.osk.models.rocketpropulsion;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.events.TimeStep;
import org.osk.models.BaseModel;
import org.osk.ports.AnalogPort;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for a controller component providing 2 analog output ports.
 * Control Function is time dependent and is currently hard coded inside model.
 *
 * @author J. Eickhoff
 */
public class EngineController extends BaseModel {

//	@Inject Logger LOG;
	@Inject @TimeStep Double timeStep;

	/** Commandeable control value. */
	private double controlRangeMax;
	private double controlRangeMin;
	private double controlValue1Nom;
	private double controlValue2Nom;
	private double controlValueActual;
	private double controlValue1;
	private double controlValue2;
	private double localtime;

	private static final String TYPE = "EngineController";
	private static final String SOLVER = "none";

    public EngineController() {
         super(TYPE, SOLVER);
    }

    @PostConstruct
    public void init() {
        /* Computation of derived initialization parameters. */
        localtime = 0.0;
        controlValueActual = controlRangeMax;
        controlValue1 = controlValueActual * controlValue1Nom;
        controlValue2 = controlValueActual * controlValue2Nom;
    }
    
    public ImmutablePair<AnalogPort, AnalogPort> timeStep() {

        /**
         * Time dependent functionality of the controller has to be computed
         * here. For a PID controller e.g. the integrative and differential
         * part. For this simple controller signal reduction is computed keeping
         * relation of signals to each other.
         */
    	localtime = localtime + timeStep;
        if (localtime == timeStep) {
            return createNewControlSignal();
        }
        controlValueActual = controlValueActual - 0.001*timeStep/0.5;
        if (controlValueActual < controlRangeMin) {
            controlValueActual = controlRangeMin;
        }
        controlValue1 = controlValueActual * controlValue1Nom;
        controlValue2 = controlValueActual * controlValue2Nom;
        return createNewControlSignal();
    }

    public ImmutablePair<AnalogPort, AnalogPort> regulStep(AnalogPort controlPort1, AnalogPort controlPort2) {
        controlPort1.setAnalogValue(controlValue1);
        controlPort2.setAnalogValue(controlValue2);
        return new ImmutablePair<AnalogPort, AnalogPort> (controlPort1, controlPort2);
    }

	public ImmutablePair<AnalogPort, AnalogPort> createNewControlSignal() {
		AnalogPort controlPort1 = new AnalogPort();
		AnalogPort controlPort2 = new AnalogPort();
		controlPort1.setAnalogValue(controlValue1);
		controlPort2.setAnalogValue(controlValue2);
		return new ImmutablePair<AnalogPort, AnalogPort> (controlPort1, controlPort2);
	}

	//----------------------------------------
    // Methods added for JMX monitoring	

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
    public double getControlValue1Nom() {
		return controlValue1Nom;
	}

	public void setControlValue1Nom(double controlValue1Nom) {
		this.controlValue1Nom = controlValue1Nom;
	}

	@ManagedAttribute
	public double getControlValue2Nom() {
		return controlValue2Nom;
	}

	public void setControlValue2Nom(double controlValue2Nom) {
		this.controlValue2Nom = controlValue2Nom;
	}

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
    
}
