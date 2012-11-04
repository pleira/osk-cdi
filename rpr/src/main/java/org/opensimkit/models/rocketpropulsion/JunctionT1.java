/*
 * JunctionT1.java
 *
 * Created on 8. Juli 2007, 21:24
 *
 *  Model definition for a pipe junction:
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
package org.opensimkit.models.rocketpropulsion;

import java.io.FileWriter;
import java.io.IOException;

import org.opensimkit.BaseModel;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.ports.PureGasPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a pipe junction. Model for connecting two pure gas
 * pipes. Computation only coveres adding of mass flow rates.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @version 1.4
 * @since 2.4.0
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
    private int    startflag;

    /** Fluid parameters of in- and outbound flows (p, t, mflow). */
    @Manipulatable private double pinLeft;
    @Manipulatable private double tinLeft;
    @Manipulatable private double mfinLeft;
    @Manipulatable private double pinRight;
    @Manipulatable private double tinRight;
    @Manipulatable private double mfinRight;
    @Manipulatable private double pout;
    @Manipulatable private double tout;
    @Manipulatable private double mfout;

    private static final String TYPE      = "JunctionT1";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 1.0E6;
    private static final double MINTSTEP  = 1.0E-6;
    private static final int    TIMESTEP  = 0;
    private static final int    REGULSTEP = 0;

    @Manipulatable private PureGasPort inputPortLeft;
    @Manipulatable private PureGasPort inputPortRight;
    @Manipulatable private PureGasPort outputPort;

    /**
     * Creates a new instance of the Junction.
     *
     * @param name Name of the instance.
     */
    public JunctionT1(final String name) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }

    public JunctionT1(final String name, PureGasPort inputPortLeft, 
    		PureGasPort inputPortRight, PureGasPort outputPort) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        this.outputPort = outputPort;
        this.inputPortLeft = inputPortLeft;
        this.inputPortRight = inputPortRight;
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
        /* Initializing split factor. */
        splitfactor = 0.5;
        oldsplitfactor = 0.5;
        stepsize = 0.1;
        startflag = 0;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.debug("% {} TimeStep-Computation", name);

        return 0;
    }


    @Override
    public int iterationStep() {
        String fluid;
        int    result;
        double newSplitfactor;

        LOG.debug("% {} IterationStep-Computation", name);

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

            outputPort.setFluid(fluid);
            outputPort.setPressure(pout);
            outputPort.setTemperature(tout);
            outputPort.setMassflow(mfout);

            return 0;
        } else if (mfinLeft == 0.0) {
            fluid = inputPortRight.getFluid();
            mfout = mfinRight;
            pout  = pinRight;
            tout  = tinRight;

            outputPort.setFluid(fluid);
            outputPort.setPressure(pout);
            outputPort.setTemperature(tout);
            outputPort.setMassflow(mfout);

            return 0;
        } else if (mfinRight == 0.0) {
            fluid = inputPortLeft.getFluid();
            mfout = mfinLeft;
            pout  = pinLeft;
            tout  = tinLeft;

            outputPort.setFluid(fluid);
            outputPort.setPressure(pout);
            outputPort.setTemperature(tout);
            outputPort.setMassflow(mfout);

            return 0;
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

        outputPort.setFluid(fluid);
        outputPort.setPressure(pout);
        outputPort.setTemperature(tout);
        outputPort.setMassflow(mfout);

        // Return value indicates to mesh whether hydraulic cond. is fulfilled.
        return result;
    }


    @Override
    public int backIterStep() {
        int result;

        result = 0;

        LOG.debug("% {} BackIteration-Computation", name);

        if (outputPort.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on port 2 cannot"
                    + " be handled!", name);
        //    nonResumeFlag = 1;
            result = 1;
        }
        if (outputPort.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on port 2 cannot"
                    + " be handled!", name);
        //    nonResumeFlag = 1;
            result = 1;
        }

        inputPortLeft.setBoundaryFluid(outputPort.getBoundaryFluid());
        inputPortLeft.setBoundaryPressure(-999999.99);
        inputPortLeft.setBoundaryTemperature(-999999.99);
        inputPortLeft.setBoundaryMassflow(
            outputPort.getBoundaryMassflow() * splitfactor);

        inputPortRight.setBoundaryFluid(outputPort.getBoundaryFluid());
        inputPortRight.setBoundaryPressure(-999999.99);
        inputPortRight.setBoundaryTemperature(-999999.99);
        inputPortRight.setBoundaryMassflow(
            outputPort.getBoundaryMassflow() * (1.0 - splitfactor));

        return result;
    }


    @Override
    public int regulStep() {
        LOG.debug("% {} RegulStep-Computation", name);
        return 0;
    }


    @Override
    public int save(final FileWriter outFile) throws IOException {
        outFile.write("JunctionT1: '" + name + "'" + SimHeaders.NEWLINE);
        return 0;
    }
}
