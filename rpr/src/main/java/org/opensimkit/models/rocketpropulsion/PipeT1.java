/*
 * PipeT1.java
 *
 * Created on 8. July 2007, 21:30
 *
 *  Model definition for a pipe.
 *
 *  Input Port --+<- Sect1-><- Sect2-><- Sect3->.....<- Sect10->+-- Output Port
 *
 *  Pipe computes the following phenomena:
 *    - Pressure loss of fluid passing through pipe
 *    - Heat transfer from pipe wall to fluid. Pipe is discretized into 10
 *      sections with separate wall temperature.
 *  Assumed is that pipe can be recognized adiabatic to the environment.
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
 *        Peter Heinrich  peterhe@student.ethz.ch
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
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.ports.PureGasPort;
import org.opensimkit.MaterialProperties;
import org.opensimkit.SimHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a pipe.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @version 1.4
 * @since 2.4.0
 */
public class PipeT1 extends BaseModel {
    /** Logger instance for the PipeT1. */
    private static final Logger LOG = LoggerFactory.getLogger(PipeT1.class);
    /** Diameter of pipe. */
    @Manipulatable private double innerDiameter;
    /** Length of pipe. */
    @Manipulatable private double length;
    /** Length specific mass. */
    @Manipulatable private double specificMass;
    /** Specific. heat capacity. */
    @Manipulatable private double specificHeatCapacity;
    /** Roughness of pipe inner surface. */
    @Manipulatable private double surfaceRoughness;
    /** Array of temperature of pipe elements. */
    @Manipulatable private double temperatures[] = new double[10];
    /** Array of heat flow from wall to fluid for pipe elements. */
    @Manipulatable private double qHFlow[] = new double[10];
    /** Heat transfer coefficient between pipe wall and fluid. */
    private double alfa;
    /** Mass of one pipe element (pipe consists of 10 elements). */
    @Manipulatable private double massPElem;
    /** Static temperature of pipe entering fluid in timestep. */
    private double tstatin;

    /** Parameters of in- and outflowing fluid. */
    @Manipulatable private double pin;
    @Manipulatable private double tin;
    @Manipulatable private double mfin;
    @Manipulatable private double pout;
    @Manipulatable private double tout;
    @Manipulatable private double pUpBackiter;
    @Manipulatable private double tUpBackiter;
    @Manipulatable private double mfUpBackiter;

    private static final String TYPE      = "PipeT1";
    private static final String SOLVER    = "Euler";
    private static final double MAXTSTEP  = 10.0;
    private static final double MINTSTEP  = 0.001;
    private static final int    TIMESTEP  = 1;
    private static final int    REGULSTEP = 0;

    @Manipulatable private PureGasPort inputPort;
    @Manipulatable private PureGasPort outputPort;


    /**
     * Creates a new instance of the pipe.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
    public PipeT1(final String name) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }

    public PipeT1(final String name, PureGasPort inputPort, PureGasPort outputPort) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        this.inputPort = inputPort;
        this.outputPort = outputPort;
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
        /* Mass of one pipe element. */
        massPElem = specificMass * length / 10;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        String     fluid;
        double     CP, Q, DTF, DTB;
        int        J;

        LOG.debug("% {} TimeStep-Computation", name);

        pin  = inputPort.getPressure();
        tin  = inputPort.getTemperature();
        mfin = inputPort.getMassflow();
        fluid = inputPort.getFluid();

        /* Skip time step computation if no flow in pipe. */
        if (mfin <= 1.E-6) {
            return 0;
        }

        CP = 5223.2;

        /**********************************************************************/
        /*                                                                    */
        /*    Temperature change of pipe                                      */
        /*                                                                    */
        /*    Section for computation of temperature change of pipe.          */
        /*    Coding is an approximation splitting pipe into 10 subsections   */
        /*    with individual temperature.                                    */
        /*                                                                    */
        /*    J= Number of pipe section                                       */
        /*                                                                    */
        /*    Fluid properties (viscosity ...) are considered to be           */
        /*    constant over entire pipe, same as Nusselt number and thus      */
        /*    heat transfer coefficient Alfa.                                 */
        /*                                                                    */
        /*    Computation of the heat transfer numbers,                       */
        /*    XI,Prandtl-Zahl, Nusselt, ALFA.                                 */
        /*    please refer to [1] section 3.3.3.2, Eq..(3.1) ff               */
        /*                                                                    */
        /*    Change: Approximation of Laval number at pipe outlet to be same */
        /*    as L1 at pipe inlet port.                                       */
        /*                                                                    */
        /*     Computation of heatflow from pipe to fluid                     */
        /*     for each pipe of the 10 pipe elements.                         */
        /*                                                                    */
        /**********************************************************************/
        for (J = 0; J < 10; J++) {
            qHFlow[J]=alfa*3.1415*innerDiameter*length
                    * (temperatures[J]-tstatin)/10;
            Q = qHFlow[J]*tStepSize;

            /******************************************************************/
            /*                                                                */
            /*    Computation of delta T for fluid and new fluid temperature  */
            /*    for each pipe element                                       */
            /*                                                                */
            /******************************************************************/
            DTF=qHFlow[J] / (mfin * CP);
            tstatin = tstatin + DTF;

            /******************************************************************/
            /*                                                                */
            /*    Computation of delta T of each pipe section                 */
            /*    and computation of new pipe temp. for each section          */
            /*                                                                */
            /******************************************************************/
            DTB=Q/(massPElem*specificHeatCapacity);
            temperatures[J]=temperatures[J]-DTB;
        }
        return 0;
    }


    @Override
    public int iterationStep() {
        String     fluid;
        double RSPEZ, CP;
        double GESCH, RE, REbound, LA;
        double zeta;
        double XI, PR, NU, DTF;
        int    J;

        /** Fluid material properties for heat transfer computations. */
        MaterialProperties Helium = new MaterialProperties();

        LOG.debug("% {} IterationStep-Computation", name);

        pin  = inputPort.getPressure();
        tin  = inputPort.getTemperature();
        mfin = inputPort.getMassflow();
        fluid = inputPort.getFluid();

        /* Skip iteration step computation if no flow in pipe. */
        if (mfin <= 1.E-6) {
            pout  = pin;
            tout  = tin;

            outputPort.setFluid(fluid);
            outputPort.setPressure(pout);
            outputPort.setTemperature(tout);
            outputPort.setMassflow(mfin);

            return 0;
        }

        RSPEZ = 2077;
        CP = 5223.2;

        // FIXME
        double PK = org.opensimkit.Helium.HELIUM(pin, tin, Helium);

        GESCH = 4. * mfin
                / (Helium.DICHTE*3.1415*Math.pow(innerDiameter, 2));

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of friction factor Lambda according to              */
        /*    Colebrook formula                                               */
        /*    See manuscript "Industrielle Aerodynamik" p.11                  */
        /*    Institut fuer Aero- und Gasdynamik                              */
        /*    Universitt Stuttgart, Pfaffenwaldring, 7000 Stuttgart 80, 1986  */
        /*                                                                    */
        /**********************************************************************/
        RE=GESCH*innerDiameter/Helium.NUE;

        if (surfaceRoughness >= 5.E-02) {
            surfaceRoughness = 5.E-02;
        }

        if (RE < 1.) {
            LA=0.;
        } else {
            REbound = 1000.;
            for (J = 0; J < 6; J++) {
                REbound=Math.pow((16.*(Math.log10(2.51*0.125
                        /Math.sqrt(REbound)+surfaceRoughness/3.71))),2.);
            }

            if (RE <= REbound) { //laminar flow
                LA = 64. / RE;
            } else { //turbulent flow
                LA = 0.0515;
                for (J = 0; J < 6; J++) {
                    LA=0.25/Math.pow((Math.log10(2.51/RE/Math.sqrt(LA)
                            + surfaceRoughness/3.71)),2.);
                } //numeric approx. for Colebrook formula.
            }
        }

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of ressure loss in pipe.                            */
        /*    See manuscript "Industrielle Aerodynamik" p.11                  */
        /*                                                                    */
        /**********************************************************************/
        zeta = LA * length / innerDiameter;

        pout=pin-(Helium.DICHTE/2.)*Math.pow(GESCH,2)*zeta;

        /**********************************************************************/
        /*                                                                    */
        /*    Temperature change of flow                                      */
        /*                                                                    */
        /*    Section for computation of temperature change of fluid          */
        /*    when passing pipe element with different temperature.           */
        /*    Material properties of fluid and heat transfer coefficients     */
        /*    are considered to be constant over each of the 10 pipe sections.*/
        /*                                                                    */
        /*    J= Number of pipe section                                       */
        /*                                                                    */
        /*    Fluid properties (viscosity ...) are considered to be           */
        /*    constant over entire pipe, same as Nusselt number and thus      */
        /*    heat transfer coefficient Alfa.                                 */
        /*                                                                    */
        /*    Computation of the heat transfer numbers,                       */
        /*    XI,Prandtl-Zahl, Nusselt, ALFA.                                 */
        /*    please refer to [1] section 3.3.3.2, Eq..(3.1) ff               */
        /*                                                                    */
        /*    Change: Approximation of Laval number at pipe outlet to be same */
        /*    as L1 at pipe inlet port.                                       */
        /*                                                                    */
        /**********************************************************************/

        if (RE > 2.E6) {
            LOG.info("Re number exceeding upper limit");
            LOG.debug("Re number exceeding upper limit");
            RE = 2.E6;
        } else if (RE < 2300.) {
            /* Setting RE to 1000 here leads to NU = 0.0 and alfa = 0.0 below
             * thus resulting in computation of transferred heat qHFlow=0.0.
             * This is necessary, since the formulae used below are not precise
             * enough for ranges of RE<2300 and computed heat transfers will
             * lead to buggy fluid temperatures.
             */
            RE = 1000.;
        }

        XI=Math.pow((1.82*(Math.log10(RE))-1.64),(-2));

        PR=CP*Helium.ETA/Helium.LAMBDA;

        NU=(XI/8)*(RE-1000.)*PR /(1+12.7*(Math.sqrt(XI/8))
                * (Math.pow(PR,(2/3))-1))
                * (1+Math.pow((innerDiameter/length), (2/3)));

        alfa=NU*Helium.LAMBDA/innerDiameter;

        /**********************************************************************/
        /*                                                                    */
        /*     Computation of heatflow from pipe wall to fluid for each of    */
        /*     the 10 pipe sections.                                          */
        /*                                                                    */
        /**********************************************************************/

        /* Static pipe inlet temperature. Required for timestep computation. */
        tstatin = tin;

        for (J = 0; J < 10; J++) {
            qHFlow[J]=alfa * Math.PI * innerDiameter * length
            * (temperatures[J]-tstatin)/10;

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of fluid temperature change and new fluid temp.     */
        /*                                                                    */
        /**********************************************************************/
            DTF=qHFlow[J] / (mfin * CP);
            tstatin=tstatin+DTF;
        }
        /* Pipe consists of 10 Elements.
         * tout = input temp of fictive 11th element. */
        tout = tstatin;

        if ((tout - tin) > 10.0) {
            LOG.info("Temp. change > 10 deg. in pipe '{}'", name);
            LOG.debug("Temp. change > 10 deg. in pipe '{}'", name);
        }

        /**********************************************************************/
        /*                                                                    */
        /*   Massflow at outlet                                               */
        /*                                                                    */
        /**********************************************************************/

        outputPort.setFluid(fluid);
        outputPort.setPressure(pout);
        outputPort.setTemperature(tout);
        outputPort.setMassflow(mfin);

        LOG.debug(" -> pout := {}", pout);
        LOG.debug(" -> tout := {}", tout);
        LOG.debug(" -> mfin/out := {}", mfin);

        return 0;
    }


    @Override
    public int backIterStep() {
        int result;

        result = 0;

        LOG.debug("% {} BackIteration-Computation", name);

        if (outputPort.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on port 1 cannot"
                    + " be handled!", name);
            result = 1;
        }
        if (outputPort.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on port 1 cannot"
                    + " be handled!", name);
            result = 1;
        }

        mfUpBackiter = outputPort.getBoundaryMassflow();
        pUpBackiter = outputPort.getBoundaryPressure();
        tUpBackiter = outputPort.getBoundaryTemperature();
        LOG.debug(" -> pUpBackiter := {}", pUpBackiter);
        LOG.debug(" -> tUpBackiter := {}", tUpBackiter);
        LOG.debug(" -> mfUpBackiter := {}", mfUpBackiter);

        inputPort.setBoundaryFluid(outputPort.getBoundaryFluid());
        inputPort.setBoundaryPressure(-999999.99);
        inputPort.setBoundaryTemperature(-999999.99);
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
        outFile.write("PipeT1: '" + name + "'" + SimHeaders.NEWLINE);
        return 0;
    }
}
