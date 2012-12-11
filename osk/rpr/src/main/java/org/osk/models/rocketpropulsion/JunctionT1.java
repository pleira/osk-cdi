/*
 * JunctionT1.java
 *
 * Created on 8. Juli 2007, 21:24
 *
 *  Model definition for a pipe junction:
t this.name = name;  
 *
 *         Left Input Port   --+\
 *                               \
 *                                >--+--- Output Port
 *                               /
 *         Right Input Port  --+/
 *
 *  Junction computes the following phenomena:
 *    - Ideal mixture temperature of outflow from data of both incoming flows
 *    - Outflow massflow is sum of incoming flows
 *    - Pressure of outflow is minimum of incoming flows.
 *  Assumed is that both incoming fluids are the same pure gases
 *
 *
 *  Component physics for rocket tank pressurization systems are taken from:
 *
 *    [1]
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
 *      Peter Heinrich  peterhe@student.ethz.ch
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
import org.osk.config.SimHeaders;
import org.osk.models.BaseModel;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for a pipe junction. Model for connecting two pure gas
 * pipes. Computation only coveres adding of mass flow rates.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 */

public class JunctionT1 extends BaseModel {
	/** Logger instance for the JunctionT1. */
	private static final Logger LOG = LoggerFactory.getLogger(JunctionT1.class);
	/** Relation of inbound massflows in actual iteration step. */
	private double splitfactor;
	/** Relation of inbound massflows in previous iteration step. */
	private double oldsplitfactor;
	/** Hydraulic residuum of actual iteration step. */
	private double hydrError;
	/** Hydraulic residuum of previous iteration step. */
	private double oldhydrError;
	/** Stepsize for iterating inbound mass flow ratios. */
	private double stepsize;
	/** Flag needed for mesh iteration. */
	private int startflag;

	/** Fluid parameters of in- and outbound flows (p, t, mflow). */
	private double pinLeft;
	private double tinLeft;
	private double mfinLeft;
	private double pinRight;
	private double tinRight;
	private double mfinRight;
	private double pout;
	private double tout;
	private double mfout;

	private static final String TYPE = "JunctionT1";
	private static final String SOLVER = "none";
    
    public JunctionT1() {
        super(TYPE, SOLVER);
    }

    public void init(String name) {
    	this.name = name;
        /* Computation of derived initialization parameters. */
        /* Initializing split factor. */
        splitfactor = 0.5;
        oldsplitfactor = 0.5;
        stepsize = 0.1;
        startflag = 0;
    }

    public FluidPort  iterationStep(FluidPort inputPortLeft, FluidPort inputPortRight) {
        String fluid;
        int    result;
        double newSplitfactor;

        pinLeft   = inputPortLeft.getPressure();
        tinLeft   = inputPortLeft.getTemperature();
        mfinLeft  = inputPortLeft.getMassflow();
        pinRight  = inputPortRight.getPressure();
        tinRight  = inputPortRight.getTemperature();
        mfinRight = inputPortRight.getMassflow();

        /* Skip iteration, if one or both of the ports has a mass flow of 0.0.
         */
        if (mfinLeft == 0.0 && mfinRight == 0.0) {
            fluid = inputPortLeft.getFluid();
            mfout = 0.0;
            pout  = (pinLeft + pinRight) / 2.;
            tout  = (tinLeft + tinRight) / 2.;
            return createOutputPort(fluid);
        } else if (mfinLeft == 0.0) {
            fluid = inputPortRight.getFluid();
            mfout = mfinRight;
            pout  = pinRight;
            tout  = tinRight;
            return createOutputPort(fluid);
        } else if (mfinRight == 0.0) {
            fluid = inputPortLeft.getFluid();
            mfout = mfinLeft;
            pout  = pinLeft;
            tout  = tinLeft;
            return createOutputPort(fluid);
        }

        //Readjust mass flow requests for backward iteration,
        //if one the pressure difference in the incoming ports is not
        //neglectable.
        //
        hydrError = pinLeft - pinRight;

        /** Hydraulic condition not yet fulfilled. */
        if (Math.abs(hydrError) >= 100000.0 * SimHeaders.epsrel) {
            if (startflag == 0) {
                splitfactor = oldsplitfactor + stepsize; // Computing new factor
                oldhydrError = hydrError;
                startflag = 1;
            } else {
                // Computing new factor
                newSplitfactor = splitfactor
                    + hydrError
                    * (splitfactor - oldsplitfactor) / (oldhydrError-hydrError);
                oldhydrError = hydrError;
                oldsplitfactor = splitfactor;
                splitfactor = newSplitfactor;
            }
            result = -1;
        } else {
            // Hydraulic condition
            // fulfilled
            startflag = 1;
            result = 0;
        }

        fluid = inputPortLeft.getFluid();
        mfout = mfinLeft + mfinRight;
        pout  = (pinLeft + pinRight) / 2.;
        tout  = (tinLeft * mfinLeft
                + tinRight * mfinRight)
                / (mfinLeft + mfinRight);


        // FIXME Is this comment now right?
        // Return value indicates to mesh whether hydraulic cond. is fulfilled.
        return createOutputPort(fluid);
    }

    public ImmutablePair<FluidPort, FluidPort> backIterStep(FluidPort outputPort) {
        if (outputPort.getBoundaryPressure() >= 0.0) {
            LOG.error("Pressure request on port 2 cannot be handled!");
        }
        if (outputPort.getBoundaryTemperature() >= 0.0) {
            LOG.error("Temp. request on port 2 cannot be handled!");
        }
        FluidPort inputPortLeft = BoundaryUtils.createBoundaryPort(outputPort.getBoundaryFluid(), splitfactor * outputPort.getBoundaryMassflow());
        FluidPort inputPortRight = BoundaryUtils.createBoundaryPort(outputPort.getBoundaryFluid(), (1 - splitfactor) * outputPort.getBoundaryMassflow());
        return new ImmutablePair<FluidPort, FluidPort>(inputPortLeft, inputPortRight);
    }

	public FluidPort createOutputPort(String fluid) {
		FluidPort outputPort = new FluidPort();
		outputPort.setFluid(fluid);
		outputPort.setPressure(pout);
		outputPort.setTemperature(tout);
		outputPort.setMassflow(mfout);
		return outputPort;
	}

    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@ManagedAttribute
	public double getPinLeft() {
		return pinLeft;
	}

	public void setPinLeft(double pinLeft) {
		this.pinLeft = pinLeft;
	}

	@ManagedAttribute
	public double getTinLeft() {
		return tinLeft;
	}

	public void setTinLeft(double tinLeft) {
		this.tinLeft = tinLeft;
	}

	@ManagedAttribute
	public double getMfinLeft() {
		return mfinLeft;
	}

	public void setMfinLeft(double mfinLeft) {
		this.mfinLeft = mfinLeft;
	}

	@ManagedAttribute
	public double getPinRight() {
		return pinRight;
	}

	public void setPinRight(double pinRight) {
		this.pinRight = pinRight;
	}

	@ManagedAttribute
	public double getTinRight() {
		return tinRight;
	}

	public void setTinRight(double tinRight) {
		this.tinRight = tinRight;
	}

	@ManagedAttribute
	public double getMfinRight() {
		return mfinRight;
	}

	public void setMfinRight(double mfinRight) {
		this.mfinRight = mfinRight;
	}

	@ManagedAttribute
	public double getTout() {
		return tout;
	}

	public void setTout(double tout) {
		this.tout = tout;
	}

	@ManagedAttribute
	public double getMfout() {
		return mfout;
	}

	public void setMfout(double mfout) {
		this.mfout = mfout;
	}
    
}
