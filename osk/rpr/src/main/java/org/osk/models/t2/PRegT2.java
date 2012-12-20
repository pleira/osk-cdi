package org.osk.models.t2;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.models.BaseModel;
import org.osk.models.PressureRegulator;
import org.osk.ports.FluidPort;


/**
 * Another model definition for a gas dome pressure regulator.
 *
 * @author P. Pita
 */
public class PRegT2 extends BaseModel implements PressureRegulator {
	
	/** Diameter of pressure regul. */
	private double innerDiameter;
	/** Length of pressure regul. */
	private double length;
	/** Mass of pressure regul. */
	private double mass;
	/** Specific heat capacity. */
	private double specificHeatCapacity;
	/** Coefficients of pressure loss polynomial approximation. */
	private double[] pcoeff = new double[4];
	/** Temperature of pressure regul. elements. */
	private double temperature;

	private static final String TYPE = "PRegT2";
	private static final String SOLVER = "";

	
    public PRegT2() {
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
	public int propagate(double tStepSize, FluidPort inputPort) {
		return 0;
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
	
    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

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
	public double getMass() {
		return mass;
	}

	@Override
	public void setMass(double mass) {
		this.mass = mass;
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
	public double[] getPcoeff() {
		return pcoeff;
	}

	@Override
	public void setPcoeff(double[] pcoeff) {
		this.pcoeff = pcoeff;
	}

	@Override
	@ManagedAttribute
	public double getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

}
