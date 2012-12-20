package org.osk.models.t2;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.models.BaseModel;
import org.osk.models.Filter;
import org.osk.ports.FluidPort;

/**
 * Another model definition for a gas filter.
 * 
 * @author P. Pita
 */
public class FilterT2 extends BaseModel implements Filter {
	
 	/** Diameter of filter. */
	private double innerDiameter;
	/** Length of filter. */
	private double length;
	/** Length specific mass. */
	private double specificMass;
	/** Specific. heat capacity. */
	private double specificHeatCapacity;
	/** Temperature of filter elements. */
	private double filterTemperature;
	/** Reference pressure loss. */
	private double referencePressureLoss;
	/** Corresponding mass flow for ref. pressure loss. */
	private double referenceMassFlow;

	/** Mass of filter. */
	private double mass;
	
	private static final String TYPE = "FilterT2";
	private static final String SOLVER = "none";

	
	public FilterT2() {
		super(TYPE, SOLVER);
	}

	@Override
	public void init(String name) {
		this.name = name;
	}

	@Override
	public FluidPort calculateOutletMassFlow(FluidPort inputPort) {
		return createOutputPort(inputPort);
	}
	
	@Override
	public void propagate(final double tStepSize, FluidPort inputPort) {

	}
	
	@Override
	public FluidPort backIterStep(FluidPort outputPort) {
		// Filters just have to transfer the amount asked from the pipes, etc, 
		// no modification is done
		return outputPort;
	}

	@Override
	public FluidPort createOutputPort(FluidPort inputPort) {
		FluidPort outputPort = new FluidPort(
		inputPort.getFluid(),
		inputPort.getPressure()/1.01,
		inputPort.getTemperature(),
		inputPort.getMassflow());
		return outputPort;
	}
	
	// ----------------------------------------
	// Methods added for JMX monitoring

	@Override
	@ManagedAttribute
	public double getInnerDiameter() {
		return innerDiameter;
	}

	@Override
	public void setInnerDiameter(double innerDiameter) {
		this.innerDiameter = innerDiameter;
	}

	@Override
	@ManagedAttribute
	public double getLength() {
		return length;
	}

	@Override
	public void setLength(double length) {
		this.length = length;
	}

	@Override
	@ManagedAttribute
	public double getSpecificMass() {
		return specificMass;
	}

	@Override
	public void setSpecificMass(double specificMass) {
		this.specificMass = specificMass;
	}

	@Override
	@ManagedAttribute
	public double getSpecificHeatCapacity() {
		return specificHeatCapacity;
	}

	@Override
	public void setSpecificHeatCapacity(double specificHeatCapacity) {
		this.specificHeatCapacity = specificHeatCapacity;
	}

	@Override
	@ManagedAttribute
	public double getTemperature() {
		return filterTemperature;
	}

	@Override
	public void setTemperature(double temperature) {
		this.filterTemperature = temperature;
	}

	@Override
	@ManagedAttribute
	public double getReferencePressureLoss() {
		return referencePressureLoss;
	}

	@Override
	public void setReferencePressureLoss(double referencePressureLoss) {
		this.referencePressureLoss = referencePressureLoss;
	}

	@Override
	@ManagedAttribute
	public double getReferenceMassFlow() {
		return referenceMassFlow;
	}

	@Override
	public void setReferenceMassFlow(double referenceMassFlow) {
		this.referenceMassFlow = referenceMassFlow;
	}

	@Override
	@ManagedAttribute
	public double getMass() {
		return mass;
	}

	@Override
	public void setMass(double mass) {
		this.mass = mass;
	}

}
