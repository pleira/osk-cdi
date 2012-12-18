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
import org.osk.numeric.NumericalUtils;
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
	
	/** Fuel flow at ingnition [kg/s]. */
	private double ignitionFuelFlow;
	/** Ox flow at ingnition [kg/s]. */
	private double ignitionOxidizerFlow;
	/** Altitude above ground [ m ] */
	private double altitude; 
	/** Requested fuel flow [kg/s] */
	private double requestedFuelFlow;
	/** Requested ox flow [kg/s] */
	private double requestedOxFlow;

	private static final String TYPE = "Engine";
	private static final String SOLVER = "none";
  
    public Engine() {
        super(TYPE, SOLVER);
    }

    public void init(String name) {
    	this.name = name;  
        requestedFuelFlow = ignitionFuelFlow;
        requestedOxFlow = ignitionOxidizerFlow;
    }


	/* Computes Engine thrust [ N ] */
    public Vector3D computeThrust(FluidPort inputPortFuel, FluidPort inputPortOxidizer) throws OskException {
        final double mfin0 = inputPortFuel.getMassflow();
        final double mfin1 = inputPortOxidizer.getMassflow();

        requestedFuelFlow = mfin0;
        requestedOxFlow = mfin1;

        /* Skip time step computation if no oxidizer or fuel inflow is zero: */
        if (mfin0 == 0.0 || mfin1 == 0.0) {
        	final double thrust = 100.0; // FIXME
            return new Vector3D(thrust, 0, 0);
        }   
 
    	/*    Computing thrust as function of propellant mass flow,       */
    	/*    characteristic velocity and thrust factor                   */

    	/* Mixture ratio Oxidizer to fuel */
    	final double OF = mfin1 / mfin0;

    	/*get chamber pressure, just average value of inflow pressures */
    	/*Note: Real chamber pressure depends mainly on combustion efficiency */
    	final double pin0 = inputPortFuel.getPressure();
    	final double pin1 = inputPortOxidizer.getPressure();
    	final double pc = 0.5*( pin0 + pin1 );
    	final double thrust = calculateThrust(OF, mfin0 + mfin1, pc);
    	// ISP = cstar * etaCstar * cf * etaCf / 9.81; // FIXME: Why does the model use g at sea level?

    	return new Vector3D(thrust, 0, 0);

    }

	private double calculateThrust(final double OF, final double mass, final double chamberPressure) throws OskException {
		
    	final double cstar = mixtureCharacteristicVelocity(OF);

    	/*Isentropic exponent of combustion gas [-], function of OF*/
    	final double k = isentropicExponentOfCombustionGas(OF);

    	/** Nozzle area ratio(assumed) */
    	final double areaRatio = 100.0;
    	/** Combustion efficiency */
    	final double etaCstar = 0.94;
    	/** Thrust factor efficiency */
    	final double etaCf = 0.99;

    	/*Nozzle exit pressure pe*/
    	final double pe = NumericalUtils.newton( k, areaRatio, chamberPressure);
    	if (pe == 0.0) {
    		throw new OskException(new DummyLocalizable("% Engine: Iteration for nozzle exit " +
    				"pressure, no solution found"));
    	}
    	/*Check for flow separation in nozzle flow: Summerfield pe<0.4*pa */
    	final double pa = atmosphere.getAirPressure(altitude);
    	if ( pe < 0.4*pa ) {
    		throw new OskException(new DummyLocalizable("% Engine: Flow separation in nozzle. " +
    				"Thrust value is not realistic"));
    	} 
    	final double cf = thrustFactor(chamberPressure, pe, k, pa, areaRatio);

    	final double thrust = mass * cstar * etaCstar * cf * etaCf;
		return thrust;
	}

	private double isentropicExponentOfCombustionGas(final double OF) {
		return - 7E-07*Math.pow(OF,6) - 0.0001*Math.pow(OF,5)
    			+ 0.0034*Math.pow(OF,4) - 0.0324*Math.pow(OF,3)
    			+ 0.1493*Math.pow(OF,2) - 0.3251*OF + 1.5081;
	}

	private double thrustFactor(final double pc, final double pe,
			final double k, final double pa, final double areaRatio) {
		/* Source: Space Propulsion Analysis and Design p.112 3.129 */
		return Math.pow((((2*k*k)/(k-1))*Math.pow((2/(k+1)),((k+1)/(k-1)))*
    			(1-Math.pow((pe/pc),((k-1)/k)))),0.5)+(pe-pa)*areaRatio/pc;
	}

	private double mixtureCharacteristicVelocity(final double OF) {
    	/******************************************************************/
    	/*    characteristic velocity of ox and fuel mixture              */
    	/*    function of mixture ratio                                   */
    	/*    Polynom for cstar calculated with CEA Gordon McBride        */
    	/*    ( http://www.grc.nasa.gov/WWW/CEAWeb/ )                     */
    	/*    Polynom valid for chamber pressures of approx. 20 bar       */
    	/******************************************************************/
    	/* Characteristic velociity [ m/s ] */
		return  - 0.1481 * Math.pow(OF,6) + 4.3126 * Math.pow(OF,5)
    			- 50.87  * Math.pow(OF,4) + 309.5  * Math.pow(OF,3)
    			- 1011.1 * Math.pow(OF,2) + 1549.9 * OF + 880.12;
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
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double alt) {
		this.altitude = alt;
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
	
}
