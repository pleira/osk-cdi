package org.osk.models;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.ports.FluidPort;

public interface Pipe {

	void init(String name);

	FluidPort calculateOutletMassFlow(FluidPort inputPort);

	void propagate(final double tStepSize, final FluidPort inputPort);

	FluidPort createOutputPort(FluidPort inputPort);

	// This are for initialization of the numerical models
	// FIXME: most values are specific to a solution algorithm, so they should be out
	@ManagedAttribute
	double getInnerDiameter();

	void setInnerDiameter(double innerDiameter);

	@ManagedAttribute
	double getLength();

	void setLength(double length);

	@ManagedAttribute
	double getSpecificMass();

	void setSpecificMass(double specificMass);

	@ManagedAttribute
	double getSpecificHeatCapacity();

	void setSpecificHeatCapacity(double specificHeatCapacity);

	@ManagedAttribute
	double getSurfaceRoughness();

	void setSurfaceRoughness(double surfaceRoughness);

	@ManagedAttribute
	double[] getTemperatures();

	void setTemperatures(double[] temperatures);


}