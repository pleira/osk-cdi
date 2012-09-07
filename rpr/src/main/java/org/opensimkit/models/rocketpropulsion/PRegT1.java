/*
 * PRegT1.java
 *
 * Created on 8. Juli 2007, 21:31
 *
 *  Model definition for a gas dome pressure regulator.
 *
 *
 *                           +--+
 *                           |  |
 *                           |  \
 *                           |   \
 *                    ,------+ |  +
 *                +----+-+-----|--|+
 * Input Port ->-+|    >|+---- +  |+
 *                +----+  \+-+-|--|+
 *                     |   | | | /
 *                     +---+ |  /
 *                       |   |  |
 *                       |   +--+
 *                       v
 *                Output Port
 *
 *
 *  The pressure regulator object computes the following phenomena:
 *    - Throtteling of gas (pure gas) including real gas effects.
 *    - Outlet pressure is computed as polinomial function (up to 3rd degree)
 *      of the inlet pressure. Polynome coefficients are read in from input
 *      deck.
 *    - Heat transfer from pressure regulator housing to fluid.
 *  Assumed is that the pressure reg. can be recognized adiabatic to
 *  the environment.
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
import org.opensimkit.HeliumJKC;
import org.opensimkit.Kernel;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.MaterialProperties;
import org.opensimkit.SimHeaders;
import org.opensimkit.ports.PureGasPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a gas dome pressure regulator.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @version 1.4
 * @since 2.4.0
 */
public class PRegT1 extends BaseModel {
    /** Logger instance for the PRegT1. */
    private static final Logger LOG = LoggerFactory.getLogger(PRegT1.class);
    /** Diameter of pressure regul. */
    @Manipulatable private double innerDiameter;
    /** Length of pressure regul. */
    @Manipulatable private double length;
    /** Mass of pressure regul. */
    @Manipulatable private double mass;
    /** Specific heat capacity. */
    @Manipulatable private double specificHeatCapacity;
    /** Coefficients of pressure loss polynomial approximation. */
    @Manipulatable private double[] pcoeff = new double[4];
    /** Temperature of pressure regul. elements. */
    @Manipulatable private double temperature;
    /** Heat flow from wall to fluid for pressure regul. elements. */
    private double qHFlow;
    /** Heat transfer coefficient between pressure regul. housing and fluid. */
    private double alfa;
    /** Temperature of fluid expanded in pressure reg. in timestep. */
    private double tstatin;

    /** Internal variables of in- and outflow. */
    @Manipulatable private double pin;
    @Manipulatable private double tin;
    @Manipulatable private double mfin;
    @Manipulatable private double pout;
    @Manipulatable private double tout;
    @Manipulatable private double pUpBackiter;
    @Manipulatable private double tUpBackiter;
    @Manipulatable private double mfUpBackiter;

    private static final String TYPE      = "PRegT1";
    private static final String SOLVER    = "Euler";
    private static final double MAXTSTEP  = 10.0;
    private static final double MINTSTEP  = 0.001;
    private static final int    TIMESTEP  = 1;
    private static final int    REGULSTEP = 0;

    @Manipulatable private PureGasPort inputPort;
    @Manipulatable private PureGasPort outputPort;

    /** Creates a new instance of the Pressure Regulator.
    *
    * @param name Name of the instance.
    * @param kernel Reference to the kernel.
    */
    public PRegT1(final String name, final Kernel kernel) {
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
        // Computation of derived initialization parameters
        //---------------------------------------------------------------------
        //
        // Initializing heat flow
        qHFlow = 0.0;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        String     fluid;
        double     CP;
        double     Q;
        double     DTF;
        double     DTB;

        LOG.debug("% {} TimeStep-Computation", name);

        pin  = inputPort.getPressure();
        tin  = inputPort.getTemperature();
        mfin = inputPort.getMassflow();
        fluid = inputPort.getFluid();

        //Skip time step computation if no flow in pressure regulator
        if (mfin <= 1.E-6) {
            return 0;
        }

        CP = 5223.2;

        /**********************************************************************/
        /*                                                                    */
        /*    Section for computation of temp. change of filter itself        */
        /*                                                                    */
        /*    Gas properties of gas fluid are assumed to be contant over,     */
        /*    entire length of filter. Same applies for the Nusselt-Number,   */
        /*    and thus for heat transfer coefficient Alfa.                    */
        /*                                                                    */
        /*                                                                    */
        /**********************************************************************/
        /*                                                                    */
        /*     Computation of heatflow from regulator housing to fluid        */
        /*                                                                    */
        /**********************************************************************/
        qHFlow = alfa * 3.1415 * innerDiameter*length*(temperature-tstatin)/10;
        Q = qHFlow * tStepSize;

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of delta T for fluid and new fluid temperature      */
        /*                                                                    */
        /**********************************************************************/
        DTF = qHFlow / (mfin * CP);
        tstatin = tstatin + DTF;

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of delta T of regulator itself and new reg. temp.   */
        /*                                                                    */
        /**********************************************************************/
        DTB = Q / (mass * specificHeatCapacity);
        temperature = temperature - DTB;

        return 0;
    }


    @Override
    public int iterationStep() {
        String     fluid;
        int I, L;
        double P1, P2, param = 0, poutNew, JKC;
        double CP, DTEMP, GESCH, RE, XI, PR, NU, DTF;

        // Fluid material properties for heat transfer computations
        MaterialProperties Helium = new MaterialProperties();

        LOG.debug("% {} IterationStep-Computation", name);

        pin  = inputPort.getPressure();
        tin  = inputPort.getTemperature();
        mfin = inputPort.getMassflow();
        fluid = inputPort.getFluid();

        LOG.debug("pin : {}", pin);
        LOG.debug("tin : {}", tin);
        LOG.debug("mfin/out : {}", mfin);

        //Skip iteration step computation if no flow in pressure regulator
        if (mfin <= 1.E-6) {
            pout  = pcoeff[0] * 1E5;
            tout  = tin;

            outputPort.setFluid(fluid);
            outputPort.setPressure(pout);
            outputPort.setTemperature(tout);
            outputPort.setMassflow(mfin);

            return 0;
        }

        CP = 5223.2;

        pout = pin / 1E5;
        tout = tin;

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of the Joule-Kelvin-Coeff. of fluid, similar to     */
        /*    pressure vessel considering specific enthalpy of fluid.         */
        /*                                                                    */
        /**********************************************************************/

        I = (int) (pout / 40. + 1.);

        P1 = 0;
        P2 = 0;
        if (I <= 8) {
            for (L = 0; L < 5; L++) {
                param = HeliumJKC.JKCParams(L, I-1);
                P1 = P1 + param * Math.pow(tout, L);
                param = HeliumJKC.JKCParams(L, I);
                P2 = P2 + param * Math.pow(tout, L);
            }
        } else {
            for (L = 0; L < 5; L++) {
                param = HeliumJKC.JKCParams(L, 7);
                P1 = P1 + param * Math.pow(tout, L);
                param = HeliumJKC.JKCParams(L, 8);
                P2 = P2 + param * Math.pow(tout, L);
            }
        }
        JKC = P1 + (P2 - P1) * (pout - (I - 1) * 40) / 40;
        JKC = JKC * -1.0;

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of pressure drop in regulator as polynomial         */
        /*    interpolation. Polynomial coefficients loaded from              */
        /*    inputfile.                                                      */
        /*                                                                    */
        /**********************************************************************/
        poutNew = pcoeff[0] + pcoeff[1] * pout
                + pcoeff[2] * Math.pow(pout, 2)
                + pcoeff[3] * Math.pow(pout, 3);

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of temp. change of fluid through throttling.        */
        /*                                                                    */
        /**********************************************************************/
        DTEMP = -JKC * (pout - poutNew);

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of outlet pressure an outlet temp not yet           */
        /*    heat exchange effects btw. regulator & fluid.                   */
        /*                                                                    */
        /**********************************************************************/
        pout = poutNew * 1E5;
        tout = tout + DTEMP;
        tstatin = tout;

        Helium = org.opensimkit.Helium.HELIUM(pout, tout, Helium);

        GESCH = mfin
                * 4 / (innerDiameter * innerDiameter * Math.PI * Helium.DICHTE);

        RE = GESCH * innerDiameter / Helium.NUE;

        /**********************************************************************/
        /*                                                                    */
        /*    Temperature change of flow                                      */
        /*                                                                    */
        /*    Section for computation of temperature change of fluid          */
        /*    when passing filter with different temperature.                 */
        /*    Material properties of fluid and heat transfer coefficients     */
        /*    are considered to be constant over entire filter.              */
        /*                                                                    */
        /*    Computation of the heat transfer numbers,                       */
        /*    XI,Prandtl-Zahl, Nusselt, ALFA.                                 */
        /*    please refer to [1] section 3.3.3.2, Eq..(3.1) ff               */
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

        XI = Math.pow((1.82 * (Math.log10(RE)) - 1.64), (-2));

        PR = CP * Helium.ETA / Helium.LAMBDA;

        NU = (XI / 8) * (RE - 1000) * PR / (1 + 12.7 * (Math.sqrt(XI / 8))
                * (Math.pow(PR, (2/3))-1))
                * (1 + Math.pow((innerDiameter / length), (2/3)));

        alfa = NU * Helium.LAMBDA / innerDiameter;

        /**********************************************************************/
        /*                                                                    */
        /*     Computation of heat flow from pressure regulator to fluid      */
        /*                                                                    */
        /**********************************************************************/
        qHFlow=alfa*3.1415*innerDiameter*length*(temperature-tout)/10;

        /**********************************************************************/
        /*                                                                    */
        /*    Computation of fluid temperature change and new fluid temp.     */
        /*                                                                    */
        /**********************************************************************/
        DTF = qHFlow / (mfin * CP);
        tout = tout + DTF;

        if (DTF > 10.0) {
            LOG.info("Temp. change > 10 deg. in press. regulator '{}'", name);
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

        LOG.debug("pout : {}", pout);
        LOG.debug("tout : {}", tout);
        LOG.debug("mfout : {}", mfin);

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
            //    nonResumeFlag = 1;
            result = 1;
        }

        mfUpBackiter  = outputPort.getBoundaryMassflow();
        pUpBackiter = outputPort.getBoundaryPressure();
        tUpBackiter = outputPort.getBoundaryTemperature();
        LOG.debug("pUpBackiter : {}", pUpBackiter);
        LOG.debug("tUpBackiter : {}", tUpBackiter);
        LOG.debug("mfUpBackiter : {}", mfUpBackiter);

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
        outFile.write("PRegT1: '" + name + "'" + SimHeaders.NEWLINE);
        return 0;
    }
}
