package org.osk.models.t2;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.models.BaseModel;
import org.osk.models.Pipe;
import org.osk.ports.FluidPort;


/**
 * Alternative Model definition for a pipe.
 * 
 * @author P. Pita
 */

public class PipeT2 extends BaseModel implements Pipe {
	
	private static final int PARTS = 0;

	/** Diameter of pipe. */
	private double innerDiameter;
	/** Length of pipe. */
	private double length;
	/** Length specific mass. */
	private double specificMass;
	/** Specific. heat capacity. */
	private double specificHeatCapacity;
	/** Roughness of pipe inner surface. */
	private double surfaceRoughness;
	/** Array of temperature of pipe elements. */
	private double temperatures[] = new double[PARTS];
	/** Array of heat flow from wall to fluid for pipe elements. */
	private double qHFlow[] = new double[PARTS];
	/** Heat transfer coefficient between pipe wall and fluid. */
	private double alfa;
	/** Mass of one pipe element (pipe consists of 10 elements). */
	private double massPElem;
	/** Static temperature of pipe entering fluid in timestep. */
	private double tstatin;

	/** Parameters of in- and outflowing fluid. */
	private double mfin;
	private double pout;
	private double tout;

	private static final String TYPE = "PipeT2";
	private static final String SOLVER = "";

	public PipeT2() {
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
	public void propagate(final double tStepSize, final FluidPort inputPort) {
     // nothing in this model
	}

	public FluidPort createOutputPort(FluidPort inputPort) {
		FluidPort outputPort = new FluidPort(
		inputPort.getFluid(),
		inputPort.getPressure()/1.01,
		inputPort.getTemperature(),
		inputPort.getMassflow());
		return outputPort;
	}
	
	
	// -----------------------------------------------------------------------------------
	// Methods added for JMX monitoring and setting initial properties via CDI
	// Extensions

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
	public double getSurfaceRoughness() {
		return surfaceRoughness;
	}

	@Override
	public void setSurfaceRoughness(double surfaceRoughness) {
		this.surfaceRoughness = surfaceRoughness;
	}

	@Override
	@ManagedAttribute
	public double[] getTemperatures() {
		return temperatures;
	}

	@Override
	public void setTemperatures(double[] temperatures) {
		this.temperatures = temperatures;
	}


	@ManagedAttribute
	public double[] getqHFlow() {
		return qHFlow;
	}

	
	public void setqHFlow(double[] qHFlow) {
		this.qHFlow = qHFlow;
	}

	
	@ManagedAttribute
	public double getAlfa() {
		return alfa;
	}

	
	public void setAlfa(double alfa) {
		this.alfa = alfa;
	}

	
	@ManagedAttribute
	public double getMassPElem() {
		return massPElem;
	}

	
	public void setMassPElem(double massPElem) {
		this.massPElem = massPElem;
	}

	
	@ManagedAttribute
	public double getTstatin() {
		return tstatin;
	}

	
	public void setTstatin(double tstatin) {
		this.tstatin = tstatin;
	}

	
	@ManagedAttribute
	public double getMfin() {
		return mfin;
	}

	
	public void setMfin(double mfin) {
		this.mfin = mfin;
	}

	
	@ManagedAttribute
	public double getPout() {
		return pout;
	}

	
	public void setPout(double pout) {
		this.pout = pout;
	}

	
	@ManagedAttribute
	public double getTout() {
		return tout;
	}

	
	public void setTout(double tout) {
		this.tout = tout;
	}

}
