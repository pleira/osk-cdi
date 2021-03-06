/*
 *  Model definition for a gas filter.
 *
 *                 +-------------------------+
 *    inputPort --+|            :            |+-- outputPort
 *                 +-------------------------+
 *
 *  Filter computes the following phenomena:
 *    - Pressure loss of fluid passing through filter
 *    - Heat transfer from filter wall to fluid.
 *  Assumed is that filter can be recognized adiabatic to the environment.
 *
 *  Component physics for rocket tank pressurization systems are taken from:
 *
 *    [1]
 *    Eickhoff, J.:
 *    Erstellung und Programmierung eines Rechenverfahrens zur
 *    thermodynamischen Erfassung des Druckgas-Foerdersystems der
 *    ARIANE L5-Stufe und Berechnung des noetigen Heliumbedarfs zur
 *    Treibstoffoerderung.
 *    Studienarbeit am Institut fuer Thermodynamik der Luft- und Raumfahrt
 *    Universitaet Stuttgart, Pfaffenwaldring 31, 7000 Stuttgart 80, 1988
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.osk.models.t1;

import javax.inject.Inject;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.models.BaseModel;
import org.osk.models.Filter;
import org.osk.models.materials.HeliumPropertiesBuilder;
import org.osk.models.materials.MaterialProperties;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;

/**
 * Model definition for a gas filter.
 * 
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author P. Pita
 */

public class FilterT1 extends BaseModel implements Filter {
	@Inject Logger LOG; 
	
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
	/** Internal variables of in- and outflow. */
	private double pin;
	private double tin;
	private double mfin;
	private double pout;
	private double tout;

	/** Mass of filter. */
	private double mass;
	
	private static final String TYPE = "FilterT1";
	private static final String SOLVER = "none";

	final double CP = 5223.2;
	MaterialProperties helium;
	private double NU;
	
	public FilterT1() {
		super(TYPE, SOLVER);
	}

	public void init(String name) {
		this.name = name;
		/* Computation of derived initialization parameters. */
		/* Mass of filter. */
		mass = specificMass * length;
		referencePressureLoss = referencePressureLoss * 1.E5;
	}

	public FluidPort calculateOutletMassFlow(FluidPort inputPort) {
		pin = inputPort.getPressure();
		tin = inputPort.getTemperature();
		mfin = inputPort.getMassflow();
		String fluid = inputPort.getFluid();

		// Skip iteration step computation if no flow in pipe
		if (mfin <= 1.E-6) {
			pout = pin;
			tout = tin;
            return createOutputPort(fluid);
		}

		/* Pressure loss in gas filter as linear dependency of fluid flow. */
		/* Reference pressure loss & reference mass flow */
		/* are design variables read from inputfile. */

		final double DP = referencePressureLoss * mfin / referenceMassFlow;
		final double pfluid = pin - DP / 2.;
		pout = pin - DP;

		final double qHFlow = heliumHeatFlow(CP, filterTemperature, pfluid);

		/* Computation of fluid temperature change and new fluid temp. */
		final double DTF = qHFlow / (mfin * CP);
		tout = tin + DTF;

		if (DTF > 10.0) {
			LOG.warn("Temp. change > 10 deg. in filter '");
		}

		/* Massflow at outlet */
		return createOutputPort(fluid);
	}

	public void propagate(final double tStepSize, FluidPort inputPort) {
		pin = inputPort.getPressure();
		tin = inputPort.getTemperature();
		mfin = inputPort.getMassflow();

		/* Skip time step computation if no flow in filter. */
		if (mfin <= 1.E-6) {
			return;
		}


		/**********************************************************************/
		/*                                                                    */
		/* Section for computation of temp. change of filter itself */
		/*                                                                    */
		/* Gas properties of gas fluid are assumed to be contant over, */
		/* entire length of filter. Same applies for the Nusselt-Number, */
		/* and thus for heat transfer coefficient Alfa */
		/*                                                                    */
		/*                                                                    */
		/**********************************************************************/

		/* Computation of heatflow from filter housing to fluid */
		final double qHFlow = NU * helium.LAMBDA * Math.PI * length 
				* (filterTemperature - tin);
		final double Q = qHFlow * tStepSize;

		/* Computation of delta T for fluid and new fluid temperature */
		final double DTF = qHFlow / (mfin * CP);
		// FIXME: why do not use tin here? The state is lost
		//tstatin = tstatin + DTF;
		tin = tin + DTF;

		/* Computation of delta T of filter itself and new filter temp. */
		final double DTB = Q / (mass * specificHeatCapacity);
		filterTemperature = filterTemperature - DTB;

	}
	

	private double heliumHeatFlow(final double fluidT, final double wallT, final double pfluid) {
		/* Fluid material properties for heat transfer computations. */
		helium = HeliumPropertiesBuilder.build(pfluid, fluidT);

		final double GESCH = mfin * 4
				/ (innerDiameter * innerDiameter * Math.PI * helium.DENSITY);

		double RE = GESCH * innerDiameter / helium.NUE;

		/**********************************************************************/
		/* Temperature change of flow */
		/*                                                                    */
		/* Section for computation of temperature change of fluid */
		/* when passing filter with different temperature. */
		/* Material properties of fluid and heat transfer coefficients */
		/* are considered to be constant over entire filter. */
		/*                                                                    */
		/* Computation of the heat transfer numbers, */
		/* XI, Prandtl-Zahl, Nusselt, ALFA. */
		/* please refer to [1] section 3.3.3.2, Eq..(3.1) ff */
		/*                                                                    */
		/**********************************************************************/

		if (RE > 2.E6) {
			LOG.warn("Re number exceeding upper limit");
			RE = 2.E6;
		} else if (RE < 2300.) {
			/*
			 * Setting RE to 1000 here leads to NU = 0.0 and alfa = 0.0 below
			 * thus resulting in computation of transferred heat qHFlow=0.0.
			 * This is necessary, since the formulae used below are not precise
			 * enough for ranges of RE<2300 and computed heat transfers will
			 * lead to buggy fluid temperatures.
			 */
			RE = 1000.;
		}

		final double XI = Math.pow((1.82 * (Math.log10(RE)) - 1.64), (-2));

		final double PR = CP * helium.ETA / helium.LAMBDA;

		NU = (XI / 8)
				* (RE - 1000)
				* PR
				/ (1 + 12.7 * (Math.sqrt(XI / 8)) * (Math.pow(PR, (2 / 3)) - 1))
				* (1 + Math.pow((innerDiameter / length), (2 / 3)));

		/* Computation of heat flow from filter to fluid */
		final double qHFlow = NU * helium.LAMBDA * Math.PI * length * (wallT - fluidT);
		return qHFlow;
	}
	
	public FluidPort backIterStep(FluidPort outputPort) {
		// Filters just have to transfer the amount asked from the pipes, etc, 
		// no modification is done
		return outputPort;
	}

	public FluidPort createOutputPort(String fluid) {
		FluidPort outputPort = new FluidPort();
		outputPort.setFluid(fluid);
		outputPort.setPressure(pout);
		outputPort.setTemperature(tout);
		outputPort.setMassflow(mfin);
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

	@ManagedAttribute
	public double getInnerDiameter() {
		return innerDiameter;
	}

	public void setInnerDiameter(double innerDiameter) {
		this.innerDiameter = innerDiameter;
	}

	@ManagedAttribute
	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	@ManagedAttribute
	public double getSpecificMass() {
		return specificMass;
	}

	public void setSpecificMass(double specificMass) {
		this.specificMass = specificMass;
	}

	@ManagedAttribute
	public double getSpecificHeatCapacity() {
		return specificHeatCapacity;
	}

	public void setSpecificHeatCapacity(double specificHeatCapacity) {
		this.specificHeatCapacity = specificHeatCapacity;
	}

	@ManagedAttribute
	public double getTemperature() {
		return filterTemperature;
	}

	public void setTemperature(double temperature) {
		this.filterTemperature = temperature;
	}

	@ManagedAttribute
	public double getReferencePressureLoss() {
		return referencePressureLoss;
	}

	public void setReferencePressureLoss(double referencePressureLoss) {
		this.referencePressureLoss = referencePressureLoss;
	}

	@ManagedAttribute
	public double getReferenceMassFlow() {
		return referenceMassFlow;
	}

	public void setReferenceMassFlow(double referenceMassFlow) {
		this.referenceMassFlow = referenceMassFlow;
	}

	@ManagedAttribute
	public double getPin() {
		return pin;
	}

	public void setPin(double pin) {
		this.pin = pin;
	}

	@ManagedAttribute
	public double getTin() {
		return tin;
	}

	public void setTin(double tin) {
		this.tin = tin;
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

	@ManagedAttribute
	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

}
