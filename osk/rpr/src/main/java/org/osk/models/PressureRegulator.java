package org.osk.models;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.ports.FluidPort;

public interface PressureRegulator {

	void init(String name);

	FluidPort calculateOutletMassFlow(FluidPort inputPort);

	FluidPort createOutputPort(FluidPort inputPort);
    
	int propagate(final double tStepSize, FluidPort inputPort);

	@ManagedAttribute
	double getInnerDiameter();

	void setInnerDiameter(double innerDiameter);

	@ManagedAttribute
	double getLength();

	void setLength(double length);

	@ManagedAttribute
	double getMass();

	void setMass(double mass);

	@ManagedAttribute
	double getSpecificHeatCapacity();

	void setSpecificHeatCapacity(double specificHeatCapacity);

	@ManagedAttribute
	double[] getPcoeff();

	void setPcoeff(double[] pcoeff);

	@ManagedAttribute
	double getTemperature();

	void setTemperature(double temperature);

}