/*
 * Engine.java
 *
 * Created on 20. February 2009
 *
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-20
 *      File created
 *      M. Kobald, A. Bohr:
 *
 *  2009-06-10
 *      File entirely revised and cleaned.
 *      J. Eickhoff:
 *
 *  2009-07
 *      Applied diverse bug fixes together with mesh class for
 *      iteration/backiteration.
 *      J. Eickhoff
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 *
 *  2009-10-31
 *      File expanded M. Kobald:
 *      Added realistic engine physics: Calculating nonlinear thrust from cstar
 *      as a function of mixture ratio OF and thrust factor cf as a function
 *      of nozzle exit pressure and ambient pressure( as a function of altitude).
 *
 *  2009-11
 *      Added altitude input from structure position for proper 
 *      thrust computation.
 *      J. Eickhoff
 *
 *  2011-01
 *      Implemented:
 *      Altitude handover to engine via provider/subscriber mechanism 
 *      instead of dedicated port class.
 *      Thrust vector handover to ScStructure via same mechanism.
 *      Diverse minor cleanups.
 *      J. Eickhoff
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
 */
package org.opensimkit.models.rocketpropulsion;

import java.io.FileWriter;
import java.io.IOException;

import org.opensimkit.BaseModel;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.opensimkit.ports.PureLiquidPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Model definition for an engine.
 *
 * @author M. Kobald
 * @author A. Bohr
 * @author A. Brandt
 * @author J. Eickhoff
 * @version 3.0
 * @since 2.6.0
 */
public abstract class Engine extends BaseModel {
    /** Logger instance for the Engine. */
    private static final Logger LOG = LoggerFactory.getLogger(Engine.class);
    /** Reference force. */
    //@Manipulatable private double referenceForce = 0;
    /** Fuel flow at ingnition [kg/s]. */
    @Manipulatable private double ignitionFuelFlow = 0;
    /** Ox flow at ingnition [kg/s]. */
    @Manipulatable private double ignitionOxidizerFlow = 0;
    /** Altitude above ground [ m ] */
    @Manipulatable private double alt = 1600000;
    /** Specific impulse [s] */
    @Readable private double ISP;
    /** Fuel inflow pressure [Pa] */
    @Readable private double pin0;
    /** Fuel inflow temperature [K] */
    @Readable private double tin0;
    /** Ox inflow pressure [Pa] */
    @Readable private double pin1;
    /** Ox inflow temperature [K] */
    @Readable private double tin1;
    /** Fuel flow [kg/s] */
    @Readable private double mfin0;
    /** Ox flow [kg/s] */
    @Readable private double mfin1;
    /** Requested fuel flow [kg/s] */
    @Readable private double requestedFuelFlow;
    /** Requested ox flow [kg/s] */
    @Readable private double requestedOxFlow;
    /** Engine thrust [ N ] */
    @Readable private double thrust;
    /** Mixture ratio Oxidizer to fuel */
    @Readable private double OF;
    /** Local time */
    @Readable private double localtime;
    /**Temperature in [ °C ] */
    @Readable private double Ta;       
    /**Temperature in [ Pa ] */
    @Readable private double pa;
    /**Temperature in [ g/m^3 ] */
    @Readable private double rhoa;
    /**Characteristic vel. [ m/s ] */
    @Readable private double cstar;
    /**Thrust factor [ - ] */
    @Readable private double cf;
    /**Nozzle area ratio(assumed) */
    @Readable private double areaRatio = 100.0;
    /**Combustion efficiency   */
    @Readable private double etaCstar= 0.94;
    /**Thrust factor efficiency*/
    @Readable private double etaCf = 0.99;
    /**Thrust: Value and direction vector in SC body frame. Vector components in [N] */
    @Readable private double[] thrustVector = new double[4];

    @Readable private double pc;
    @Readable private double pe;
    @Readable private double k;

    private static final String TYPE      = "Engine";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 10.0;
    private static final double MINTSTEP  = 0.001;
    private static final int    TIMESTEP  = 1;
    private static final int    REGULSTEP = 0;

    @Manipulatable private final PureLiquidPort inputPortFuel;
    @Manipulatable private final PureLiquidPort inputPortOxidizer;
    
    /*----------------------------------------------------------------------
    Note! The variable(s)
        alt
    is(are) used in subsequent code lines.
    Please assure that the according variable(s) is(are) provided to this 
    model via the provider/subscriber mechanism by specifying an according 
    subscription entry in the simulation input file.
    ------------------------------------------------------------------------
    Note! The variable(s)
        thrustVector[]
    is(are) computed for use by other models in subsequent code lines.
    Please assure that the according variable(s) is(are) handed over to
    subscribers by specifying an according provision entry in the simulation 
    input file.
    ----------------------------------------------------------------------*/


    /**
     * Creates a new instance of the engine.
     *
     * @param name Name of the instance.
     */
    public Engine(final String name) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        inputPortFuel = null;
        inputPortOxidizer = null;
    }

    public Engine(final String name, PureLiquidPort inputPortOxidizer, 
    		PureLiquidPort inputPortFuel) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        this.inputPortOxidizer = inputPortOxidizer;
        this.inputPortFuel = inputPortFuel;
    }
    
    public double getIgnitionFuelFlow() {
		return ignitionFuelFlow;
	}

	public void setIgnitionFuelFlow(double ingnitionFuelFlow) {
		this.ignitionFuelFlow = ingnitionFuelFlow;
	}

	public double getIgnitionOxidizerFlow() {
		return ignitionOxidizerFlow;
	}

	public void setIgnitionOxidizerFlow(double ingnitionOxidizerFlow) {
		this.ignitionOxidizerFlow = ingnitionOxidizerFlow;
	}

	public double getAlt() {
		return alt;
	}

	public void setAlt(double alt) {
		this.alt = alt;
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
        LOG.debug("% {} Init-Computation", name);
        localtime = 0.0;
        thrustVector[0] = 0;
        thrustVector[1] = 1;
        thrustVector[2] = 0;
        thrustVector[3] = 0;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        localtime = localtime + 0.5;

        LOG.debug("% {} TimeStep-Computation", name);

        /* Fuel: */
        mfin0 = inputPortFuel.getMassflow();
        /* Oxidizer: */
        mfin1 = inputPortOxidizer.getMassflow();

        /* Remember inflows for next backiteration step.... */
        requestedFuelFlow = mfin0;
        requestedOxFlow = mfin1;

        //System.out.println("alt: " + alt);

        /* Skip time step computation if no oxidizer or fuel inflow is zero: */
        if (mfin0 == 0.0 || mfin1 == 0.0) {
            thrust = 100.0;
            OF = 1.0;
            cstar = 0.0;
            cf = 0.0;
            thrustVector[0] = thrust;
            thrustVector[1] = 1;
            thrustVector[2] = 0;
            thrustVector[2] = 0;
            return 0;

        } else {

            /******************************************************************/
            /*                                                                */
            /*    Simple Earth atmosphere model for pressure and temperature  */
            /*    http://www.grc.nasa.gov/WWW/K-12/airplane/atmosmet.html     */
            /*                                                                */
            /******************************************************************/
            if ( alt <= 11000 ) {   
                /* Troposphere */
                Ta = 15.04 - 0.00649*alt;
                pa = (101.29*(Math.pow((Ta+273.15)/288.08,5.256)))*1000;
                rhoa = pa/(286.9*(Ta+273.15));

            } else if ( alt > 11000 && alt < 25000 ) {  
                /* Lower Stratosphere */
                Ta = -56.46;
                pa = 22.56*(Math.exp(1.73 - 0.000157*alt))*1000;
                rhoa = pa/(286.9*(Ta+273.15));
                            
            } else if ( alt >= 25000 ) {  
                /* Upper Stratosphere */
                Ta = -131.21 + 0.00299*alt;
                pa = (2.488*(Math.pow((Ta+273.15)/216.6,-11.388)))*1000;
                rhoa = pa/(286.9*(Ta+273.15));
            
            } else {
                LOG.error("% {} Engine: Negative altitude", name);
                return -1;
            }
            
            /******************************************************************/
            /*                                                                */
            /*    Computing thrust as function of propellant mass flow,       */
            /*    characteristic velocity and thrust factor                   */
            /*                                                                */
            /******************************************************************/
            /** Mixture ratio Oxidizer to fuel */
            OF = mfin1 / mfin0;

            /*get chamber pressure, just average value of inflow pressures */
            /*Note: Real chamber pressure depends mainly on combustion efficiency */
            pin0 = inputPortFuel.getPressure();
            pin1 = inputPortOxidizer.getPressure();
            pc = 0.5*( pin0 + pin1 );

            /******************************************************************/
            /*                                                                */
            /*    characteristic velocity of ox and fuel mixture              */
            /*    function of mixture ratio                                   */
            /*    Polynom for cstar calculated with CEA Gordon McBride        */
            /*    ( http://www.grc.nasa.gov/WWW/CEAWeb/ )                     */
            /*    Polynom valid for chamber pressures of approx. 20bar        */
            /*                                                                */
            /******************************************************************/
            cstar = - 0.1481*Math.pow(OF,6) + 4.3126*Math.pow(OF,5)
                    - 50.87*Math.pow(OF,4)  + 309.5*Math.pow(OF,3)
                    - 1011.1*Math.pow(OF,2) + 1549.9*OF + 880.12;

            /*Isentropic exponent of combustion gas [-], function of OF*/
            k = - 7E-07*Math.pow(OF,6) - 0.0001*Math.pow(OF,5)
                    + 0.0034*Math.pow(OF,4) - 0.0324*Math.pow(OF,3)
                    + 0.1493*Math.pow(OF,2) - 0.3251*OF + 1.5081;

            /*Nozzle exit pressure pe*/
            pe = newton( k, areaRatio, pc);

            /*Check for flow separation in nozzle flow: Summerfield pe<0.4*pa */
            if ( pe < 0.4*pa ) {
                LOG.debug("% {} Engine: Flow separation in nozzle. " +
                       "Thrust value is not realistic", name);
                /* Thrust factor cf */
                /* Source: Space Propulsion Analysis and Design p.112 3.129 */
                cf = 1.0; /*No thrust from nozzle due to flow separation*/
            } else {
                cf = Math.pow((((2*k*k)/(k-1))*Math.pow((2/(k+1)),((k+1)/(k-1)))*
                    (1-Math.pow((pe/pc),((k-1)/k)))),0.5)+(pe-pa)*areaRatio/pc;
            }
            
            thrust = (mfin0 + mfin1) * cstar * etaCstar * cf * etaCf;
            ISP = cstar * etaCstar * cf * etaCf / 9.81;

            thrustVector[0] = thrust;
            thrustVector[1] = 1;
            thrustVector[2] = 0;
            thrustVector[2] = 0;
            return 0;
        }
    }

    
    /***************************************************************************
    *                                                                          *
    *    Calculate nozzle exit pressure pe from chamber pressure pc            *
    *    assuming adiabatic isentropic 1D-flow                                 *
    *    Iterative Newton-method                                               *
    *                                                                          *
    ***************************************************************************/
    public double newton(final double k, final double areaRatio,
                         final double pc) {
        double newp;
        double oldp = 0.0;
        double t1, t2;      /*t1, t2 temporary variables                */
        double F;           /*Formulae for nozzle area ratio            */
        /* Source: Space Propulsion Analysis and Design p.102 3.100     */

        double dF;          /*Derivative of F to (pe/pc): dF/d(pe/pc)   */
        int i = 0;

        newp = 1E3 / pc ; //Start value for iteration
        while ( (Math.abs(oldp - newp) > 0.00001 ) || ( i == 0 ) )
        {
            i = i + 1;
            oldp = newp;
            t1 = Math.pow(( 2 / ( k + 1 )),( 1 / ( k - 1 )))
                    * Math.pow((( k + 1 )/( k - 1 )),( -0.5 ));
            t2 = Math.pow(oldp,( - 1 / k ))*
                    Math.pow(( 1 - Math.pow( oldp, (( k - 1 )/ k ))),( -0.5 ));
            F = areaRatio / t1 - t2;

            t2 = ( - 1 / k)*Math.pow(oldp,( -1 - 1 / k))*
                    Math.pow(( 1 - Math.pow(oldp,(( k - 1 ) / k))),( -0.5 ));
            dF = areaRatio / t1 - t2 + Math.pow(oldp,( -1 / k)) *
                    ( - (( k - 1 ) / ( 2 * k ))
                    *Math.pow(oldp,((( k - 1 ) / k ) - 1 ))*
                    Math.pow(( 1 - Math.pow(oldp,(( k - 1 ) / k ))),( -1.5 )));
              newp = oldp - F / dF;
              if (i >= 100 || newp < 0.0 ) {
                  newp=1E2/pc; /*Assuming 1mbar pe*/
                  LOG.debug("% {} Engine: Iteration for nozzle exit " +
                          "pressure, no solution found", name);
                  break; /* oder abbrechen return 0*/
              }
        }
        pe = newp * pc;
        return pe;
    }

    @Override
    public int iterationStep() {
        LOG.debug("% {} IterationStep-Computation", name);

        /* Fuel: */
        pin0  = inputPortFuel.getPressure();
        tin0  = inputPortFuel.getTemperature();
        mfin0 = inputPortFuel.getMassflow();
        /* Oxidizer: */
        pin1  = inputPortOxidizer.getPressure();
        tin1  = inputPortOxidizer.getTemperature();
        mfin1 = inputPortOxidizer.getMassflow();

        /* Check whether boundary condition is fulfilled.....
         * (Equality of set and received mass flow). */
        if ((Math.abs(mfin0 - requestedFuelFlow) < 0.005)
                && (Math.abs(mfin1 - requestedOxFlow) < 0.005)) {
            /* Iteration of upward propulsion system successful. */
            return 0;
        } else {
            /* Otherwise reinitiate another propulsion system iteration... */
            requestedFuelFlow = mfin0;
            requestedOxFlow = mfin1;
            return -1;
        }
    }


    @Override
    public int backIterStep() {
        LOG.debug("% {} BackIteration-Computation", name);

        if (localtime == 0.0) {
            requestedFuelFlow = ignitionFuelFlow;
            requestedOxFlow = ignitionOxidizerFlow;
        }

        /* Fuel: */
        inputPortFuel.setBoundaryFluid("Fuel");
        inputPortFuel.setBoundaryPressure(-999999.99);
        inputPortFuel.setBoundaryTemperature(-999999.99);
        inputPortFuel.setBoundaryMassflow(requestedFuelFlow);
        /* Oxidizer: */
        inputPortOxidizer.setBoundaryFluid("Oxidizer");
        inputPortOxidizer.setBoundaryPressure(-999999.99);
        inputPortOxidizer.setBoundaryTemperature(-999999.99);
        inputPortOxidizer.setBoundaryMassflow(requestedOxFlow);

        return 0;
    }


    @Override
    public int regulStep() {
        LOG.debug("% {} RegulStep-Computation", name);

        return 0;
    }


    @Override
    public int save(final FileWriter outFile) throws IOException {
        outFile.write("Engine: '" + name + "'" + SimHeaders.NEWLINE);

        return 0;
    }
}
