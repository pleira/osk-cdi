package org.osk.models;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.errors.OskException;
import org.osk.ports.FluidPort;

public interface Engine {

	void init(String name);

	/* Computes Engine thrust [ N ] */
	Vector3D computeThrust(FluidPort inputPortFuel, FluidPort inputPortOxidizer)
			throws OskException;

	@ManagedAttribute
	double getIgnitionFuelFlow();

	void setIgnitionFuelFlow(double ingnitionFuelFlow);

	@ManagedAttribute
	double getIgnitionOxidizerFlow();

	void setIgnitionOxidizerFlow(double ingnitionOxidizerFlow);

	@ManagedAttribute
	double getAltitude();

	void setAltitude(double alt);

	@ManagedAttribute
	double getRequestedFuelFlow();

	void setRequestedFuelFlow(double requestedFuelFlow);

	@ManagedAttribute
	double getRequestedOxFlow();

	void setRequestedOxFlow(double requestedOxFlow);

}