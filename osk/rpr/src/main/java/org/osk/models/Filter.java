package org.osk.models;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.ports.FluidPort;

public interface Filter {

	void init(String name);

	FluidPort calculateOutletMassFlow(FluidPort inputPort);

	void propagate(final double tStepSize, FluidPort inputPort);

	FluidPort backIterStep(FluidPort outputPort);

	FluidPort createOutputPort(FluidPort inputPort);

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
	double getTemperature();

	void setTemperature(double temperature);

	@ManagedAttribute
	double getReferencePressureLoss();

	void setReferencePressureLoss(double referencePressureLoss);

	@ManagedAttribute
	double getReferenceMassFlow();

	void setReferenceMassFlow(double referenceMassFlow);

	@ManagedAttribute
	double getMass();

	void setMass(double mass);

}