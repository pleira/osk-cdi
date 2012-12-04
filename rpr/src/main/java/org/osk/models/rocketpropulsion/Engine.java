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

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.events.D4Value;
import org.opensimkit.events.ECEFpv;
import org.opensimkit.events.Thrust;
import org.opensimkit.models.BaseModel;
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

	private static final Logger LOG = LoggerFactory.getLogger(Engine.class);
	/** Reference force. */
	// private double referenceForce = 0;
	/** Fuel flow at ingnition [kg/s]. */
	private double ignitionFuelFlow = 0;
	/** Ox flow at ingnition [kg/s]. */
	private double ignitionOxidizerFlow = 0;
	/** Altitude above ground [ m ] */
	private double alt; // = 1600000;
	/** Specific impulse [s] */
	private double ISP;
	/** Fuel inflow pressure [Pa] */
	private double pin0;
	/** Fuel inflow temperature [K] */
	private double tin0;
	/** Ox inflow pressure [Pa] */
	private double pin1;
	/** Ox inflow temperature [K] */
	private double tin1;
	/** Fuel flow [kg/s] */
	private double mfin0;
	/** Ox flow [kg/s] */
	private double mfin1;
	/** Requested fuel flow [kg/s] */
	private double requestedFuelFlow;
	/** Requested ox flow [kg/s] */
	private double requestedOxFlow;
	/** Engine thrust [ N ] */
	private double thrust;
	/** Mixture ratio Oxidizer to fuel */
	private double OF;
	/** Local time */
	private double localtime;
	/** Temperature in [ °C ] */
	private double Ta;
	/** Temperature in [ Pa ] */
	private double pa;
	/** Temperature in [ g/m^3 ] */
	private double rhoa;
	/** Characteristic vel. [ m/s ] */
	private double cstar;
	/** Thrust factor [ - ] */
	private double cf;
	/** Nozzle area ratio(assumed) */
	private double areaRatio = 100.0;
	/** Combustion efficiency */
	private double etaCstar = 0.94;
	/** Thrust factor efficiency */
	private double etaCf = 0.99;
	/**
	 * Thrust: Value and direction vector in SC body frame. Vector components in
	 * [N]
	 */
	private double[] thrustVector = new double[4];

	private double pc;
	private double pe;
	private double k;

	private static final String TYPE = "Engine";
	private static final String SOLVER = "none";
	private static final double MAXTSTEP = 10.0;
	private static final double MINTSTEP = 0.001;

	private final PureLiquidPort inputPortFuel;
	private final PureLiquidPort inputPortOxidizer;
  
    @Inject @Thrust Event<D4Value> events;
    
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

    public Engine(final String name, PureLiquidPort inputPortOxidizer, 
    		PureLiquidPort inputPortFuel) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP);
        this.inputPortOxidizer = inputPortOxidizer;
        this.inputPortFuel = inputPortFuel;
    }

    @Override
    @PostConstruct
    public void init() {
        LOG.info("% {} Init-Computation", name);
        localtime = 0.0;
        thrustVector[0] = 0;
        thrustVector[1] = 1;
        thrustVector[2] = 0;
        thrustVector[3] = 0;
    	completeConnections();
    }

    void completeConnections() {
    	inputPortFuel.setToModel(this);
        inputPortOxidizer.setToModel(this);
    	LOG.info("completeConnections for " + name + ", (" + inputPortFuel.getName()  + "," + inputPortOxidizer.getName() + ")" );
    }

    @Override
    public int timeStep(final double time, final double tStepSize) {
        localtime = localtime + 0.5;

        LOG.info("% {} TimeStep-Computation", name);

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
                LOG.info("% {} Engine: Flow separation in nozzle. " +
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
            
            events.fire(new D4Value(thrustVector));
            
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
                  LOG.info("% {} Engine: Iteration for nozzle exit " +
                          "pressure, no solution found", name);
                  break; /* oder abbrechen return 0*/
              }
        }
        pe = newp * pc;
        return pe;
    }

    @Override
    public int iterationStep() {
        LOG.info("% {} IterationStep-Computation", name);

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
        LOG.info("% {} BackIteration-Computation", name);

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
    
	public void altitudeHandler(@Observes ECEFpv pv) {
		alt = pv.getAltitude();
	}

	
	//----------------------------------------
    // Methods added for JMX monitoring	

    
    @ManagedAttribute
    public double getIgnitionFuelFlow() {
		return ignitionFuelFlow;
	}
	public void setIgnitionFuelFlow(double ingnitionFuelFlow) {
		this.ignitionFuelFlow = ingnitionFuelFlow;
	}

    @ManagedAttribute
	public double getIgnitionOxidizerFlow() {
		return ignitionOxidizerFlow;
	}
	public void setIgnitionOxidizerFlow(double ingnitionOxidizerFlow) {
		this.ignitionOxidizerFlow = ingnitionOxidizerFlow;
	}

	@ManagedAttribute
	public double getAlt() {
		return alt;
	}
	public void setAlt(double alt) {
		this.alt = alt;
	}

	@ManagedAttribute
	public double getISP() {
		return ISP;
	}

	public void setISP(double iSP) {
		ISP = iSP;
	}
	@ManagedAttribute
	public double getPin0() {
		return pin0;
	}

	public void setPin0(double pin0) {
		this.pin0 = pin0;
	}
	@ManagedAttribute
	public double getTin0() {
		return tin0;
	}

	public void setTin0(double tin0) {
		this.tin0 = tin0;
	}
	@ManagedAttribute
	public double getPin1() {
		return pin1;
	}

	public void setPin1(double pin1) {
		this.pin1 = pin1;
	}
	@ManagedAttribute
	public double getTin1() {
		return tin1;
	}

	public void setTin1(double tin1) {
		this.tin1 = tin1;
	}
	@ManagedAttribute
	public double getMfin0() {
		return mfin0;
	}

	public void setMfin0(double mfin0) {
		this.mfin0 = mfin0;
	}
	@ManagedAttribute
	public double getMfin1() {
		return mfin1;
	}

	public void setMfin1(double mfin1) {
		this.mfin1 = mfin1;
	}
	@ManagedAttribute
	public double getRequestedFuelFlow() {
		return requestedFuelFlow;
	}

	public void setRequestedFuelFlow(double requestedFuelFlow) {
		this.requestedFuelFlow = requestedFuelFlow;
	}
	@ManagedAttribute
	public double getRequestedOxFlow() {
		return requestedOxFlow;
	}

	public void setRequestedOxFlow(double requestedOxFlow) {
		this.requestedOxFlow = requestedOxFlow;
	}
	@ManagedAttribute
	public double getThrust() {
		return thrust;
	}

	public void setThrust(double thrust) {
		this.thrust = thrust;
	}
	@ManagedAttribute
	public double getOF() {
		return OF;
	}

	public void setOF(double oF) {
		OF = oF;
	}
	@ManagedAttribute
	public double getLocaltime() {
		return localtime;
	}

	public void setLocaltime(double localtime) {
		this.localtime = localtime;
	}
	@ManagedAttribute
	public double getTa() {
		return Ta;
	}

	public void setTa(double ta) {
		Ta = ta;
	}
	@ManagedAttribute
	public double getPa() {
		return pa;
	}

	public void setPa(double pa) {
		this.pa = pa;
	}
	@ManagedAttribute
	public double getRhoa() {
		return rhoa;
	}

	public void setRhoa(double rhoa) {
		this.rhoa = rhoa;
	}
	@ManagedAttribute
	public double getCstar() {
		return cstar;
	}

	public void setCstar(double cstar) {
		this.cstar = cstar;
	}
	@ManagedAttribute
	public double getCf() {
		return cf;
	}

	public void setCf(double cf) {
		this.cf = cf;
	}
	@ManagedAttribute
	public double getAreaRatio() {
		return areaRatio;
	}

	public void setAreaRatio(double areaRatio) {
		this.areaRatio = areaRatio;
	}
	@ManagedAttribute
	public double getEtaCstar() {
		return etaCstar;
	}

	public void setEtaCstar(double etaCstar) {
		this.etaCstar = etaCstar;
	}
	@ManagedAttribute
	public double getEtaCf() {
		return etaCf;
	}

	public void setEtaCf(double etaCf) {
		this.etaCf = etaCf;
	}
	@ManagedAttribute
	public double[] getThrustVector() {
		return thrustVector;
	}

	public void setThrustVector(double[] thrustVector) {
		this.thrustVector = thrustVector;
	}
	@ManagedAttribute
	public double getPc() {
		return pc;
	}

	public void setPc(double pc) {
		this.pc = pc;
	}
	@ManagedAttribute
	public double getPe() {
		return pe;
	}

	public void setPe(double pe) {
		this.pe = pe;
	}
	@ManagedAttribute
	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}
	
}
