/*-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *-----------------------------------------------------------------------------
 */
package org.osk.models.rocketpropulsion;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.errors.OskException;
import org.osk.models.BaseModel;
import org.osk.models.environment.Atmosphere;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;

import com.sun.org.glassfish.gmbal.ManagedAttribute;
/**
 * Model definition for an engine.
 *      Added realistic engine physics: Calculating nonlinear thrust from cstar
 *      as a function of mixture ratio OF and thrust factor cf as a function
 *      of nozzle exit pressure and ambient pressure( as a function of altitude).

 * @author M. Kobald
 * @author A. Bohr
 * @author A. Brandt
 * @author J. Eickhoff
 * @author P. Pita
 *
 */

public class Engine extends BaseModel {

	@Inject Logger LOG;
	@Inject Atmosphere atmosphere;
	
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
	/** Nozzle area ratio(assumed) */
	private double areaRatio = 100.0;
	/** Combustion efficiency */
	private double etaCstar = 0.94;
	/** Thrust factor efficiency */
	private double etaCf = 0.99;

	/** Local time */
	double localtime;

	private static final String TYPE = "Engine";
	private static final String SOLVER = "none";
  
    public Engine() {
        super(TYPE, SOLVER);
    }

    public void init(String name) {
    	this.name = name;  
        localtime = 0.0;
    }


    public void iterationStep(FluidPort inputPortFuel, FluidPort inputPortOxidizer) throws OskException {
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
        } else {
            /* Otherwise reinitiate another propulsion system iteration... */
            requestedFuelFlow = mfin0;
            requestedOxFlow = mfin1;
        }
    }
	
    public Vector3D computeThrust(FluidPort inputPortFuel, FluidPort inputPortOxidizer) throws OskException {
        localtime = localtime + 0.5;

        /* Fuel: */
        mfin0 = inputPortFuel.getMassflow();
        /* Oxidizer: */
        mfin1 = inputPortOxidizer.getMassflow();

        /* Remember inflows for next backiteration step.... */
        requestedFuelFlow = mfin0;
        requestedOxFlow = mfin1;

        //System.out.println("alt: " + alt);
    	/** Engine thrust [ N ] */
    	final double thrust;

        /* Skip time step computation if no oxidizer or fuel inflow is zero: */
        if (mfin0 == 0.0 || mfin1 == 0.0) {
            thrust = 100.0; // FIXME
            return new Vector3D(thrust, 0, 0);

        }   
    	final double pc;
    	final double pe;
    	final double k;
    	/* Mixture ratio Oxidizer to fuel */
    	final double OF;
    	/* Temperature in [ °C ] */
    	final double Ta;
    	/* Temperature in [ Pa ] */
    	final double pa;
    	/* Temperature in [ g/m^3 ] */
    	final double rhoa;
    	/* Characteristic vel. [ m/s ] */
    	final double cstar;
    	/* Thrust factor [ - ] */
    	final double cf;

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
    	pe = NumericalUtils.newton( k, areaRatio, pc);
    	if (pe == 0.0) {
    		throw new OskException(new DummyLocalizable("% Engine: Iteration for nozzle exit " +
    				"pressure, no solution found"));
    	}
    	/*Check for flow separation in nozzle flow: Summerfield pe<0.4*pa */
    	pa = atmosphere.getAirPressure(alt);
    	if ( pe < 0.4*pa ) {
    		throw new OskException(new DummyLocalizable("% Engine: Flow separation in nozzle. " +
    				"Thrust value is not realistic"));
    		/* Thrust factor cf */
    		/* Source: Space Propulsion Analysis and Design p.112 3.129 */
    		// cf = 1.0; /*No thrust from nozzle due to flow separation*/
    	} 
    	cf = Math.pow((((2*k*k)/(k-1))*Math.pow((2/(k+1)),((k+1)/(k-1)))*
    			(1-Math.pow((pe/pc),((k-1)/k)))),0.5)+(pe-pa)*areaRatio/pc;

    	thrust = (mfin0 + mfin1) * cstar * etaCstar * cf * etaCf;
    	ISP = cstar * etaCstar * cf * etaCf / 9.81;

    	return new Vector3D(thrust, 0, 0);

    }


    public ImmutablePair<FluidPort,  FluidPort> backIterStep() {

        if (localtime == 0.0) {
            requestedFuelFlow = ignitionFuelFlow;
            requestedOxFlow = ignitionOxidizerFlow;
        }

        FluidPort inputPortFuel = createBoundaryPort("Fuel", requestedFuelFlow);
        FluidPort inputPortOxidizer = createBoundaryPort("Oxidizer", requestedOxFlow);
        return new ImmutablePair<FluidPort,  FluidPort>(inputPortFuel, inputPortOxidizer);

    }

	public FluidPort createBoundaryPort(String fluid, double requestedFlow) {
		FluidPort inputPort = new FluidPort();
        inputPort.setBoundaryFluid(fluid);
        inputPort.setBoundaryPressure(-999999.99);
        inputPort.setBoundaryTemperature(-999999.99);
        inputPort.setBoundaryMassflow(requestedFlow);
		return inputPort;
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
	
}
