/*
 * SplitT1.java
 *
 * Created on 8. Juli 2007, 21:35
 *
 *  Model definition for a pipe split:
t this.name = name;  
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
package org.osk.models.rocketpropulsion;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.models.BaseModel;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for a pipe split. Component for connecting two pure gas
 * pipes. Computation only coveres adding of mass flow rates.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
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
	
    public SplitT1() {
        super(TYPE, SOLVER);
    }

    public ImmutablePair<FluidPort, FluidPort> iterationStep(FluidPort inputPort) {
        String fluid;

        pin   = inputPort.getPressure();
        tin   = inputPort.getTemperature();
        mfin  = inputPort.getMassflow();
        fluid = inputPort.getFluid();

        pout = pin;
        tout = tin;
        if (mfin > 0.0) {
        	if (mfboundLeft == 0.0 && mfboundRight == 0.0) {
        		LOG.warn("Not initialized mfboundLeft and mfboundRight. Using 0.5.");
        		mfboundLeft = mfboundRight = 0.5;
        	}
            mfoutLeft  = mfin
                    * (mfboundLeft / (mfboundLeft + mfboundRight));
            mfoutRight = mfin
                    * (mfboundRight / (mfboundLeft + mfboundRight));
        } else {
            mfoutLeft  = 0.0;
            mfoutRight = 0.0;
        }

        FluidPort outputPortLeft =  createGasPort(fluid, mfoutLeft);
        FluidPort outputPortRight = createGasPort(fluid, mfoutRight);
        
//        LOG.info("pout : {}", pout);
//        LOG.info("tout : {}", tout);
//        LOG.info("mfoutLeft : {}", mfoutLeft);
//        LOG.info("mfoutRight : {}", mfoutRight);

        return new ImmutablePair<FluidPort, FluidPort>(outputPortLeft, outputPortRight);
    }

     public FluidPort backIterate(FluidPort outputPortLeft, FluidPort outputPortRight) {
        if (outputPortLeft.getBoundaryPressure() >= 0.0) {
            LOG.error("Pressure request on left port cannot be handled!");
        }
        if (outputPortLeft.getBoundaryTemperature() >= 0.0) {
            LOG.error("Temp. request on left port cannot be handled!");
        }
        if (outputPortRight.getBoundaryPressure() >= 0.0) {
            LOG.error("Pressure request on right port cannot be handled!");
        }
        if (outputPortRight.getBoundaryTemperature() >= 0.0) {
            LOG.error("Temp. request on right port cannot be handled!");
        }

        mfboundLeft  = outputPortLeft.getBoundaryMassflow();
        mfboundRight = outputPortRight.getBoundaryMassflow();
        mfUpBackiter = mfboundLeft + mfboundRight;
        pUpBackiter = (outputPortLeft.getBoundaryPressure()
                + outputPortRight.getBoundaryPressure()) / 2.;
        tUpBackiter = (outputPortLeft.getBoundaryTemperature()
                + outputPortRight.getBoundaryTemperature()) / 2.;
//        LOG.info("mfboundLeft : {}", mfboundLeft);
//        LOG.info("mfboundRight : {}", mfboundRight);

		FluidPort inputPort = BoundaryUtils.createBoundaryPort(
				outputPortLeft.getBoundaryFluid(), pUpBackiter, tUpBackiter, mfUpBackiter);

        return inputPort;
    }


 	public FluidPort createGasPort(String fluid, double mflow) {
		FluidPort outputPort = new FluidPort();
        outputPort.setFluid(fluid);
        outputPort.setPressure(pout);
        outputPort.setTemperature(tout);
        outputPort.setMassflow(mflow);
		return outputPort;
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

}
