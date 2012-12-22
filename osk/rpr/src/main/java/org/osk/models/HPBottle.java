package org.osk.models;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.ports.FluidPort;

public interface HPBottle {

	void init(String name);

	void calculateMassFlow(double timeStep);

	FluidPort createInputPortIter();

	FluidPort getOutputPortStatus();

	@ManagedAttribute
	double getMass();

	void setMass(double mass);

	@ManagedAttribute
	double getVolume();

	void setVolume(double volume);

	@ManagedAttribute
	double getSpecificHeatCapacity();

	void setSpecificHeatCapacity(double specificHeatCapacity);

	@ManagedAttribute
	double getPtotal();

	void setPtotal(double ptotal);

	@ManagedAttribute
	double getTtotal();

	void setTtotal(double ttotal);

	@ManagedAttribute
	String getFluid();

	void setFluid(String fluid);

	@ManagedAttribute
	double getDiam();

	void setDiam(double diam);

	@ManagedAttribute
	double getSurface();

	void setSurface(double surface);

	@ManagedAttribute
	double getTwall();

	void setTwall(double twall);

	@ManagedAttribute
	double getMtotal();

	void setMtotal(double mtotal);

	@ManagedAttribute
	double getMftotal();

	void setMftotal(double mftotal);

	@ManagedAttribute
	double getqHFlow();

	void setqHFlow(double qHFlow);

	@ManagedAttribute
	double getPinit();

	void setPinit(double pinit);

}