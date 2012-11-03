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

import java.io.FileWriter;
import java.io.IOException;
import org.opensimkit.BaseModel;
import org.opensimkit.Kernel;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.models.ports.PureGasPort;
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
    @Manipulatable private double pin;
    @Manipulatable private double tin;
    @Manipulatable private double mfin;
    @Manipulatable private double pout;
    @Manipulatable private double tout;
    @Manipulatable private double mfoutLeft;
    @Manipulatable private double mfoutRight;
    @Manipulatable private double pUpBackiter;
    @Manipulatable private double tUpBackiter;
    @Manipulatable private double mfUpBackiter;

    private static final String TYPE      = "SplitT1";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 1.0E6;
    private static final double MINTSTEP  = 1.0E-6;
    private static final int    TIMESTEP  = 0;
    private static final int    REGULSTEP = 0;

    @Manipulatable private PureGasPort inputPort;
    @Manipulatable private PureGasPort outputPortLeft;
    @Manipulatable private PureGasPort outputPortRight;


    /**
     * Creates a new instance of the split.
     *
     * @param name Name of the instance.
     */
    public SplitT1(final String name) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }

    public SplitT1(final String name, PureGasPort inputPort, 
    		PureGasPort outputPortLeft, PureGasPort outputPortRight) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        this.inputPort = inputPort;
        this.outputPortLeft = outputPortLeft;
        this.outputPortRight = outputPortRight;
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

    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.debug("% {} TimeStep-Computation", name);
        return 0;
    }


    @Override
    public int iterationStep() {
        String fluid;

        LOG.debug("% {} IterationStep-Computation", name);

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

        LOG.debug("pout : {}", pout);
        LOG.debug("tout : {}", tout);
        LOG.debug("mfoutLeft : {}", mfoutLeft);
        LOG.debug("mfoutRight : {}", mfoutRight);

        return 0;
    }


    @Override
    public int backIterStep() {
        int result;

        result = 0;

        LOG.debug("% {} BackIteration-Computation", name);

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
        LOG.debug("mfboundLeft : {}", mfboundLeft);
        LOG.debug("mfboundRight : {}", mfboundRight);
        LOG.debug("pUpBackiter : {}", pUpBackiter);
        LOG.debug("tUpBackiter : {}", tUpBackiter);
        LOG.debug("mfUpBackiter : {}", mfUpBackiter);

        inputPort.setBoundaryFluid(outputPortLeft.getBoundaryFluid());
        inputPort.setBoundaryPressure(pUpBackiter);
        inputPort.setBoundaryTemperature(tUpBackiter);
        inputPort.setBoundaryMassflow(mfUpBackiter);

        return result;
    }


    @Override
    public int regulStep() {
        LOG.debug("% {} RegulStep-Computation", name);
        return 0;
    }


    @Override
    public int save(final FileWriter outFile) throws IOException {
        outFile.write("SplitT1: '" + name + "'" + SimHeaders.NEWLINE);
        return 0;
    }
}
