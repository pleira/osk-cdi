/*
 * SplitT1.java
 *
 * Created on 8. Juli 2007, 21:35
 *
 *  Model definition for a pipe split:
 *
 *                          /+--  Left Output Port
 *                         /
 *       Input Port ---+--<
 *                         \
 *                          \+--  Right Output Port
 *
 *  Split computes the following phenomena:
 *    - Splitting the incoming flow into two outflows of equal total pressure
 *      and total temperature
 *    - Adding together eventually specified mass flow boundary conditions that
 *      are present on ports 2 and 3
 *  Assumed is that incoming fluid is pure gas.
 *
 *
 *  Component physics for rocket tank pressurization systems are taken from:
 *
 *    Eickhoff, J.:
 *    Erstellung und Programmierung eines Rechenverfahrens zur
 *    thermodynamischen Erfassung des Druckgas-Foerdersystems der
 *    ARIANE L5-Stufe und Berechnung des noetigen Heliumbedarfs zur
 *    Treibstoffoerderung.
 *    Studienarbeit am Institut fuer Thermodynamik der Luft- und Raumfahrt
 *    Universitaet Stuttgart, Pfaffenwaldring 31, 7000 Stuttgart 80, 1988
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
 *        [1]
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
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *    Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-01
 *      Diverse minor cleanups and entire textual translation to english.
 *      J. Eickhoff
 *
 *  2009-04
 *      Replaced the port array by named ports.
 *      A. Brandt
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 */
package org.opensimkit.models.rocketpropulsion;

import javax.annotation.PostConstruct;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.models.BaseModel;
import org.opensimkit.ports.PureGasPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a pipe split. Component for connecting two pure gas
 * pipes. Computation only coveres adding of mass flow rates.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @version 1.4
 * @since 2.4.0
 */
public class SplitT1 extends BaseModel {
	/** Logger instance for the SplitT1. */
	private static final Logger LOG = LoggerFactory.getLogger(SplitT1.class);
	/** Required outflow on port 1 (through backiter step). */
	private double mfboundLeft;
	/** Required outflow on port 2 (through backiter step). */
	private double mfboundRight;

	/** Fluid parameters of in- and outflow(s). */
	private double pin;
	private double tin;
	private double mfin;
	private double pout;
	private double tout;
	private double mfoutLeft;
	private double mfoutRight;
	private double pUpBackiter;
	private double tUpBackiter;
	private double mfUpBackiter;

	private static final String TYPE = "SplitT1";
	private static final String SOLVER = "none";
	private static final double MAXTSTEP = 1.0E6;
	private static final double MINTSTEP = 1.0E-6;
	private static final int TIMESTEP = 0;
	

	private PureGasPort inputPort;
	private PureGasPort outputPortLeft;
	private PureGasPort outputPortRight;

    public SplitT1(final String name, PureGasPort inputPort, 
    		PureGasPort outputPortLeft, PureGasPort outputPortRight) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP);
        this.inputPort = inputPort;
        this.outputPortLeft = outputPortLeft;
        this.outputPortRight = outputPortRight;
    }

    @Override
    @PostConstruct
    public void init() {
    	completeConnections();
    }
    
    void completeConnections() {
    	inputPort.setToModel(this);
        outputPortLeft.setFromModel(this);
        outputPortRight.setFromModel(this);
    	LOG.info("completeConnections for " + name + ", (" + inputPort.getName()  + "," + outputPortLeft.getName() + "," + outputPortRight.getName() + ")" );
    }


    @Override
    public int iterationStep() {
        String fluid;

        LOG.info("% {} IterationStep-Computation", name);

        pin   = inputPort.getPressure();
        tin   = inputPort.getTemperature();
        mfin  = inputPort.getMassflow();
        fluid = inputPort.getFluid();

        pout = pin;
        tout = tin;
        if (mfin > 0.0) {
            mfoutLeft  = mfin
                    * (mfboundLeft / (mfboundLeft + mfboundRight));
            mfoutRight = mfin
                    * (mfboundRight / (mfboundLeft + mfboundRight));
        } else {
            mfoutLeft  = 0.0;
            mfoutRight = 0.0;
        }

        outputPortLeft.setFluid(fluid);
        outputPortLeft.setPressure(pout);
        outputPortLeft.setTemperature(tout);
        outputPortLeft.setMassflow(mfoutLeft);

        outputPortRight.setFluid(fluid);
        outputPortRight.setPressure(pout);
        outputPortRight.setTemperature(tout);
        outputPortRight.setMassflow(mfoutRight);

        LOG.info("pout : {}", pout);
        LOG.info("tout : {}", tout);
        LOG.info("mfoutLeft : {}", mfoutLeft);
        LOG.info("mfoutRight : {}", mfoutRight);

        return 0;
    }


    @Override
    public int backIterStep() {
        int result;

        result = 0;

        LOG.info("% {} BackIteration-Computation", name);

        if (outputPortLeft.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on left port cannot"
                    + " be handled!", name);
            result = 1;
        }
        if (outputPortLeft.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on left port cannot"
                    + " be handled!", name);
            result = 1;
        }
        if (outputPortRight.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on right port cannot"
                    + " be handled!", name);
            result = 1;
        }
        if (outputPortRight.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on right port cannot"
                    + " be handled!", name);
            //    nonResumeFlag = 1;
            result = 1;
        }

        mfboundLeft  = outputPortLeft.getBoundaryMassflow();
        mfboundRight = outputPortRight.getBoundaryMassflow();
        mfUpBackiter = mfboundLeft + mfboundRight;
        pUpBackiter = (outputPortLeft.getBoundaryPressure()
                + outputPortRight.getBoundaryPressure()) / 2.;
        tUpBackiter = (outputPortLeft.getBoundaryTemperature()
                + outputPortRight.getBoundaryTemperature()) / 2.;
        LOG.info("mfboundLeft : {}", mfboundLeft);
        LOG.info("mfboundRight : {}", mfboundRight);
        LOG.info("pUpBackiter : {}", pUpBackiter);
        LOG.info("tUpBackiter : {}", tUpBackiter);
        LOG.info("mfUpBackiter : {}", mfUpBackiter);

        inputPort.setBoundaryFluid(outputPortLeft.getBoundaryFluid());
        inputPort.setBoundaryPressure(pUpBackiter);
        inputPort.setBoundaryTemperature(tUpBackiter);
        inputPort.setBoundaryMassflow(mfUpBackiter);

        return result;
    }


    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@ManagedAttribute
	public double getMfboundLeft() {
		return mfboundLeft;
	}

	public void setMfboundLeft(double mfboundLeft) {
		this.mfboundLeft = mfboundLeft;
	}

	@ManagedAttribute
	public double getMfboundRight() {
		return mfboundRight;
	}

	public void setMfboundRight(double mfboundRight) {
		this.mfboundRight = mfboundRight;
	}

	@ManagedAttribute
	public double getPin() {
		return pin;
	}

	public void setPin(double pin) {
		this.pin = pin;
	}

	@ManagedAttribute
	public double getTin() {
		return tin;
	}

	public void setTin(double tin) {
		this.tin = tin;
	}

	@ManagedAttribute
	public double getMfin() {
		return mfin;
	}

	public void setMfin(double mfin) {
		this.mfin = mfin;
	}

	@ManagedAttribute
	public double getPout() {
		return pout;
	}

	public void setPout(double pout) {
		this.pout = pout;
	}

	@ManagedAttribute
	public double getTout() {
		return tout;
	}

	public void setTout(double tout) {
		this.tout = tout;
	}

	@ManagedAttribute
	public double getMfoutLeft() {
		return mfoutLeft;
	}

	public void setMfoutLeft(double mfoutLeft) {
		this.mfoutLeft = mfoutLeft;
	}

	@ManagedAttribute
	public double getMfoutRight() {
		return mfoutRight;
	}

	public void setMfoutRight(double mfoutRight) {
		this.mfoutRight = mfoutRight;
	}

	@ManagedAttribute
	public double getpUpBackiter() {
		return pUpBackiter;
	}

	public void setpUpBackiter(double pUpBackiter) {
		this.pUpBackiter = pUpBackiter;
	}

	@ManagedAttribute
	public double gettUpBackiter() {
		return tUpBackiter;
	}

	public void settUpBackiter(double tUpBackiter) {
		this.tUpBackiter = tUpBackiter;
	}

	@ManagedAttribute
	public double getMfUpBackiter() {
		return mfUpBackiter;
	}

	public void setMfUpBackiter(double mfUpBackiter) {
		this.mfUpBackiter = mfUpBackiter;
	}

	@ManagedAttribute
	public PureGasPort getInputPort() {
		return inputPort;
	}

	public void setInputPort(PureGasPort inputPort) {
		this.inputPort = inputPort;
	}

	@ManagedAttribute
	public PureGasPort getOutputPortLeft() {
		return outputPortLeft;
	}

	public void setOutputPortLeft(PureGasPort outputPortLeft) {
		this.outputPortLeft = outputPortLeft;
	}

	@ManagedAttribute
	public PureGasPort getOutputPortRight() {
		return outputPortRight;
	}

	public void setOutputPortRight(PureGasPort outputPortRight) {
		this.outputPortRight = outputPortRight;
	}
}
