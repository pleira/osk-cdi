package org.osk.models.t2;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.errors.OskException;
import org.osk.models.BaseModel;
import org.osk.models.Engine;
import org.osk.models.environment.Atmosphere;
import org.osk.ports.FluidPort;

/**
 * Model definition for an engine.
 * 
 * @author P. Pita
 */
@Alternative
public class EngineT2 extends BaseModel implements Engine {

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
  
    public EngineT2() {
        super(TYPE, SOLVER);
    }

    @Override
	public void init(String name) {
    	this.name = name;  
        requestedFuelFlow = ignitionFuelFlow;
        requestedOxFlow = ignitionOxidizerFlow;
    }

	/* Computes Engine thrust [ N ] */
    @Override
	public Vector3D computeThrust(FluidPort inputPortFuel, FluidPort inputPortOxidizer) throws OskException {

    	return new Vector3D(1, 0, 0);

    }
   	
	//----------------------------------------
    // Methods added for JMX monitoring	
    
    @Override
	@ManagedAttribute
    public double getIgnitionFuelFlow() {
		return ignitionFuelFlow;
	}
	@Override
	public void setIgnitionFuelFlow(double ingnitionFuelFlow) {
		this.ignitionFuelFlow = ingnitionFuelFlow;
	}

    @Override
	@ManagedAttribute
	public double getIgnitionOxidizerFlow() {
		return ignitionOxidizerFlow;
	}
	@Override
	public void setIgnitionOxidizerFlow(double ingnitionOxidizerFlow) {
		this.ignitionOxidizerFlow = ingnitionOxidizerFlow;
	}

	@Override
	@ManagedAttribute
	public double getAltitude() {
		return altitude;
	}
	@Override
	public void setAltitude(double alt) {
		this.altitude = alt;
	}
	@Override
	@ManagedAttribute
	public double getRequestedFuelFlow() {
		return requestedFuelFlow;
	}

	@Override
	public void setRequestedFuelFlow(double requestedFuelFlow) {
		this.requestedFuelFlow = requestedFuelFlow;
	}
	@Override
	@ManagedAttribute
	public double getRequestedOxFlow() {
		return requestedOxFlow;
	}

	@Override
	public void setRequestedOxFlow(double requestedOxFlow) {
		this.requestedOxFlow = requestedOxFlow;
	}
	
}
