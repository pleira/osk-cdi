/*
 * FilterT1.java
 *
 *
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
 * Modification History:
 *
 *  2004-12-05
 *      C++ code version created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-01
 *      Diverse minor cleanups and entire textual translation to english.
 *      J. Eickhoff
 *
 *  2009-04
 *      Replaced the port array by named ports.
 *      A. Brandt
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 */
package org.opensimkit.models.rocketpropulsion;

import javax.annotation.PostConstruct;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.materials.MaterialProperties;
import org.opensimkit.models.BaseModel;
import org.opensimkit.ports.PureGasPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a gas filter.
 * 
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @version 1.4
 * @since 2.4.0
 */
public class FilterT1 extends BaseModel {
	/** Logger instance for the FilterT1. */
	private static final Logger LOG = LoggerFactory.getLogger(FilterT1.class);
	/** Diameter of filter. */
	private double innerDiameter;
	/** Length of filter. */
	private double length;
	/** Length specific mass. */
	private double specificMass;
	/** Specific. heat capacity. */
	private double specificHeatCapacity;
	/** Temperature of filter elements. */
	private double temperature;
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
	/** Heat flow from wall to fluid for filter elements. */
	private double qHFlow;
	/** Heat transfer coefficient between filter housing and fluid. */
	private double alfa;
	/** Mass of filter. */
	private double mass;
	private double pUpBackiter;
	private double tUpBackiter;
	private double mfUpBackiter;

	private static final String TYPE = "FilterT1";
	private static final String SOLVER = "none";
	private static final double MAXTSTEP = 10.0;
	private static final double MINTSTEP = 0.001;

	private PureGasPort inputPort;
	private PureGasPort outputPort;

	public FilterT1(final String name, PureGasPort inputPort,
			PureGasPort outputPort) {
		super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP);
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}

	@Override
	@PostConstruct
	public void init() {
		completeConnections();
		/* Computation of derived initialization parameters. */
		/* Initializing heat flow. */
		qHFlow = 0.0;

		/* Mass of filter. */
		mass = specificMass * length;

		referencePressureLoss = referencePressureLoss * 1.E5;

		LOG.info(" -> len := {}", length);
		LOG.info(" -> diam := {}", innerDiameter);
		LOG.info(" -> spmass := {}", specificMass);
		LOG.info(" -> cfilter := {}", specificHeatCapacity);
		LOG.info(" -> refPLoss := {}", referencePressureLoss);
		LOG.info(" -> refMassFlow := {}", referenceMassFlow);
		LOG.info(" -> tfilter := {}", temperature);
	}

	void completeConnections() {
		outputPort.setFromModel(this);
		inputPort.setToModel(this);
		LOG.info("completeConnections for " + name + ", ("
				+ inputPort.getName() + "," + outputPort.getName() + ")");
	}

	@Override
	public int timeStep(final double time, final double tStepSize) {
		String fluid;
		double CP, Q, DTF, DTB, tstatin;

		LOG.info("% {} TimeStep-Computation", name);

		pin = inputPort.getPressure();
		tin = inputPort.getTemperature();
		mfin = inputPort.getMassflow();
		fluid = inputPort.getFluid();

		/* Skip time step computation if no flow in filter. */
		if (mfin <= 1.E-6) {
			return 0;
		}

		CP = 5223.2;
		tstatin = tin;

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
		/*                                                                    */
		/* Computation of heatflow from filter housing to fluid */
		/*                                                                    */
		/**********************************************************************/

		qHFlow = alfa * Math.PI * innerDiameter * length
				* (temperature - tstatin) / 10;
		Q = qHFlow * tStepSize;

		/**********************************************************************/
		/*                                                                    */
		/* Computation of delta T for fluid and new fluid temperature */
		/*                                                                    */
		/**********************************************************************/

		DTF = qHFlow / (mfin * CP);
		tstatin = tstatin + DTF;

		/**********************************************************************/
		/*                                                                    */
		/* Computation of delta T of filter itself and new filter temp. */
		/*                                                                    */
		/**********************************************************************/

		DTB = Q / (mass * specificHeatCapacity);
		temperature = temperature - DTB;

		return 0;
	}

	@Override
	public int iterationStep() {
		double ptotal;
		double ttotal;
		double mfout;
		String fluid;
		double CP, GESCH, RE, XI, PR, NU, DTF, DP, pfluid;

		/* Fluid material properties for heat transfer computations. */
		MaterialProperties helium = new MaterialProperties();

		LOG.info("% {} IterationStep-Computation", name);

		pin = inputPort.getPressure();
		tin = inputPort.getTemperature();
		mfin = inputPort.getMassflow();
		fluid = inputPort.getFluid();

		// Skip iteration step computation if no flow in pipe
		if (mfin <= 1.E-6) {
			pout = pin;
			tout = tin;

			outputPort.setFluid(fluid);
			outputPort.setPressure(pout);
			outputPort.setTemperature(tout);
			outputPort.setMassflow(mfin);
			return 0;
		}

		CP = 5223.2;

		/**********************************************************************/
		/*                                                                    */
		/* Pressure loss in gas filter as linear dependency of */
		/* fluid flow. */
		/* Reference pressure loss & reference mass flow */
		/* are design variables read from inputfile. */
		/*                                                                    */
		/**********************************************************************/

		DP = referencePressureLoss * mfin / referenceMassFlow;
		pfluid = pin - DP / 2.;
		ptotal = pin - DP;

		ttotal = tin;

		// TODO: check pk is ok, and how it is used
		double pk = org.opensimkit.materials.Helium.HELIUM(pfluid, tin, helium);

		GESCH = mfin * 4
				/ (innerDiameter * innerDiameter * Math.PI * helium.DICHTE);

		RE = GESCH * innerDiameter / helium.NUE;

		/**********************************************************************/
		/*                                                                    */
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
			LOG.info("Re number exceeding upper limit");
			LOG.info("Re number exceeding upper limit");
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

		XI = Math.pow((1.82 * (Math.log10(RE)) - 1.64), (-2));

		PR = CP * helium.ETA / helium.LAMBDA;

		NU = (XI / 8)
				* (RE - 1000)
				* PR
				/ (1 + 12.7 * (Math.sqrt(XI / 8)) * (Math.pow(PR, (2 / 3)) - 1))
				* (1 + Math.pow((innerDiameter / length), (2 / 3)));

		alfa = NU * helium.LAMBDA / innerDiameter;

		/**********************************************************************/
		/*                                                                    */
		/* Computation of heat flow from filter to fluid */
		/*                                                                    */
		/**********************************************************************/

		qHFlow = alfa * Math.PI * innerDiameter * length * (temperature - tin);

		/**********************************************************************/
		/*                                                                    */
		/* Computation of fluid temperature change and new fluid temp. */
		/*                                                                    */
		/**********************************************************************/

		DTF = qHFlow / (mfin * CP);
		ttotal = ttotal + DTF;

		if (DTF > 10.0) {
			LOG.info("Temp. change > 10 deg. in filter '" + name);
			LOG.info("Temp. change > 10 deg. in pipe '{}'", name);
		}

		/**********************************************************************/
		/*                                                                    */
		/* Massflow at outlet */
		/*                                                                    */
		/**********************************************************************/

		mfout = mfin;

		outputPort.setFluid(fluid);
		outputPort.setPressure(ptotal);
		outputPort.setTemperature(ttotal);
		outputPort.setMassflow(mfout);

		LOG.info(" -> ptotal := {}", ptotal);
		LOG.info(" -> ttotal := {}", ttotal);
		LOG.info(" -> mfin/out := {}", mfout);

		pout = ptotal;
		tout = ttotal;
		return 0;
	}

	@Override
	public int backIterStep() {
		int result;

		result = 0;
		LOG.info("% {} BackIteration-Computation", name);

		if (outputPort.getBoundaryPressure() >= 0.0) {
			LOG.info("Error! Comp. '" + name
					+ "': Pressure request on port 1 cannot be handled!");
			// nonResumeFlag = 1;
			result = 1;
		}
		if (outputPort.getBoundaryTemperature() >= 0.0) {
			LOG.info("Error! Comp. '" + name
					+ "': Temp. request on port 1 cannot be handled!");
			// nonResumeFlag = 1;
			result = 1;
		}

		mfUpBackiter = outputPort.getBoundaryMassflow();
		pUpBackiter = outputPort.getBoundaryPressure();
		tUpBackiter = outputPort.getBoundaryTemperature();
		LOG.info(" -> pUpBackiter := {}", pUpBackiter);
		LOG.info(" -> tUpBackiter := {}", tUpBackiter);
		LOG.info(" -> mfUpBackiter := {}", mfUpBackiter);

		inputPort.setBoundaryFluid(outputPort.getBoundaryFluid());
		inputPort.setBoundaryPressure(-999999.99);
		inputPort.setBoundaryTemperature(-999999.99);
		inputPort.setBoundaryMassflow(mfUpBackiter);

		return result;
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
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
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
	public double getqHFlow() {
		return qHFlow;
	}

	public void setqHFlow(double qHFlow) {
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
	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	@ManagedAttribute
	public double getpUpBackiter() {
		return pUpBackiter;
	}

	public void setpUpBackiter(double pUpBackiter) {
		this.pUpBackiter = pUpBackiter;
	}

	@ManagedAttribute
	public double gettUpBackiter() {
		return tUpBackiter;
	}

	public void settUpBackiter(double tUpBackiter) {
		this.tUpBackiter = tUpBackiter;
	}

	@ManagedAttribute
	public double getMfUpBackiter() {
		return mfUpBackiter;
	}

	public void setMfUpBackiter(double mfUpBackiter) {
		this.mfUpBackiter = mfUpBackiter;
	}

	@ManagedAttribute
	public PureGasPort getInputPort() {
		return inputPort;
	}

	public void setInputPort(PureGasPort inputPort) {
		this.inputPort = inputPort;
	}

	@ManagedAttribute
	public PureGasPort getOutputPort() {
		return outputPort;
	}

	public void setOutputPort(PureGasPort outputPort) {
		this.outputPort = outputPort;
	}

}
