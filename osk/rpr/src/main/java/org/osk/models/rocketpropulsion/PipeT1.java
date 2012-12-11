/*
 * PipeT1.java
 *
 * Created on 8. July 2007, 21:30
 *
 *  Model definition for a pipe.
t this.name = name;  
 *
 *  Input Port --+<- Sect1-><- Sect2-><- Sect3->.....<- Sect10->+-- Output Port
 *
 *  Pipe computes the following phenomena:
 *    - Pressure loss of fluid passing through pipe
 *    - Heat transfer from pipe wall to fluid. Pipe is discretized into 10
 *      sections with separate wall temperature.
 *  Assumed is that pipe can be recognized adiabatic to the environment.
 *
 *
 *  Component physics for rocket tank pressurization systems are taken from:
 *
 *    [1]
 *    Eickhoff, J.:
 *    Erstellung und Programmierung eines Rechenverfahrens zur
 *    thermodynamischen Erfassung des Druckgas-Foerdersystems der
 *    ARIANE L5-Stufe und Berechnung des noetigen heliumbedarfs zur
 *    Treibstoffoerderung.
 *    Studienarbeit am Institut fuer Thermodynamik der Luft- und Raumfahrt
 *    Universitaet Stuttgart, Pfaffenwaldring 31, 7000 Stuttgart 80, 1988
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
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
 */
package org.osk.models.rocketpropulsion;

import javax.inject.Inject;

import org.osk.events.TimeStep;
import org.osk.models.BaseModel;
import org.osk.models.materials.HeliumPropertiesBuilder;
import org.osk.models.materials.MaterialProperties;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for a pipe.
 * 
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author P. Pita
 */

public class PipeT1 extends BaseModel {
	@Inject Logger LOG;
	@Inject @TimeStep Double tStepSize;
	
	private static final int PARTS = 10;
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

	private static final String TYPE = "PipeT1";
	private static final String SOLVER = "Euler";

	public PipeT1() {
		super(TYPE, SOLVER);
	}

	public void init(String name) {
		this.name = name;
		/* Mass of one pipe element. */
		massPElem = specificMass * length / PARTS;
	}
	
	public FluidPort iterationStep(FluidPort inputPort) {
		
		double RSPEZ, CP;
		double GESCH, RE, REbound, LA;
		double zeta;
		double XI, PR, NU, DTF;
		int J;
//		LOG.info(name);

		double pin = inputPort.getPressure();
		double tin = inputPort.getTemperature();
		mfin = inputPort.getMassflow();
		String fluid = inputPort.getFluid();

		/* Skip iteration step computation if no flow in pipe. */
		if (mfin <= 1.E-6) {
			pout = pin;
			tout = tin;
			return inputPort.clone();
		}

		RSPEZ = 2077;
		CP = 5223.2;

		// FIXME
		/** Fluid material properties for heat transfer computations. */
		MaterialProperties helium = HeliumPropertiesBuilder.build(pin, tin);

		GESCH = 4. * mfin
				/ (helium.DENSITY * 3.1415 * Math.pow(innerDiameter, 2));

		/**********************************************************************/
		/*                                                                    */
		/* Computation of friction factor Lambda according to */
		/* Colebrook formula */
		/* See manuscript "Industrielle Aerodynamik" p.11 */
		/* Institut fuer Aero- und Gasdynamik */
		/* Universitt Stuttgart, Pfaffenwaldring, 7000 Stuttgart 80, 1986 */
		/*                                                                    */
		/**********************************************************************/
		RE = GESCH * innerDiameter / helium.NUE;

		if (surfaceRoughness >= 5.E-02) {
			surfaceRoughness = 5.E-02;
		}

		if (RE < 1.) {
			LA = 0.;
		} else {
			REbound = 1000.;
			for (J = 0; J < 6; J++) {
				REbound = Math.pow(
						(16. * (Math.log10(2.51 * 0.125 / Math.sqrt(REbound)
								+ surfaceRoughness / 3.71))), 2.);
			}

			if (RE <= REbound) { // laminar flow
				LA = 64. / RE;
			} else { // turbulent flow
				LA = 0.0515;
				for (J = 0; J < 6; J++) {
					LA = 0.25 / Math.pow(
							(Math.log10(2.51 / RE / Math.sqrt(LA)
									+ surfaceRoughness / 3.71)), 2.);
				} // numeric approx. for Colebrook formula.
			}
		}

		/**********************************************************************/
		/*                                                                    */
		/* Computation of pressure loss in pipe. */
		/* See manuscript "Industrielle Aerodynamik" p.11 */
		/*                                                                    */
		/**********************************************************************/
		zeta = LA * length / innerDiameter;

		pout = pin - (helium.DENSITY / 2.) * Math.pow(GESCH, 2) * zeta;

		/**********************************************************************/
		/*                                                                    */
		/* Temperature change of flow */
		/*                                                                    */
		/* Section for computation of temperature change of fluid */
		/* when passing pipe element with different temperature. */
		/* Material properties of fluid and heat transfer coefficients */
		/* are considered to be constant over each of the 10 pipe sections. */
		/*                                                                    */
		/* J= Number of pipe section */
		/*                                                                    */
		/* Fluid properties (viscosity ...) are considered to be */
		/* constant over entire pipe, same as Nusselt number and thus */
		/* heat transfer coefficient Alfa. */
		/*                                                                    */
		/* Computation of the heat transfer numbers, */
		/* XI,Prandtl-Zahl, Nusselt, ALFA. */
		/* please refer to [1] section 3.3.3.2, Eq..(3.1) ff */
		/*                                                                    */
		/* Change: Approximation of Laval number at pipe outlet to be same */
		/* as L1 at pipe inlet port. */
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
				* (RE - 1000.)
				* PR
				/ (1 + 12.7 * (Math.sqrt(XI / 8)) * (Math.pow(PR, (2 / 3)) - 1))
				* (1 + Math.pow((innerDiameter / length), (2 / 3)));

		alfa = NU * helium.LAMBDA / innerDiameter;

		/**********************************************************************/
		/*                                                                    */
		/* Computation of heatflow from pipe wall to fluid for each of */
		/* the 10 pipe sections. */
		/*                                                                    */
		/**********************************************************************/

		/* Static pipe inlet temperature. Required for timestep computation. */
		tstatin = tin;

		for (J = 0; J < PARTS; J++) {
			qHFlow[J] = alfa * Math.PI * innerDiameter * length
					* (temperatures[J] - tstatin) / PARTS;

			/**********************************************************************/
			/*                                                                    */
			/* Computation of fluid temperature change and new fluid temp. */
			/*                                                                    */
			/**********************************************************************/
			DTF = qHFlow[J] / (mfin * CP);
			tstatin = tstatin + DTF;
		}
		/*
		 * Pipe consists of 10 Elements. tout = input temp of fictive 11th
		 * element.
		 */
		tout = tstatin;

		if ((tout - tin) > 10.0) {
			LOG.info("Temp. change > 10 deg. in pipe");
		}

		/**********************************************************************/
		/*                                                                    */
		/* Massflow at outlet */
		/*                                                                    */
		/**********************************************************************/

//		LOG.info(" -> pout := {}", pout);
//		LOG.info(" -> tout := {}", tout);
//		LOG.info(" -> mfin/out := {}", mfin);

		return createOutputPort(fluid);
	}

	public int timeStep(final FluidPort inputPort) {
		String fluid;
		double CP, Q, DTF, DTB;
		int J;

		mfin = inputPort.getMassflow();
		fluid = inputPort.getFluid();

		/* Skip time step computation if no flow in pipe. */
		if (mfin <= 1.E-6) {
			return 0;
		}

		CP = 5223.2;

		/**********************************************************************/
		/*                                                                    */
		/* Temperature change of pipe */
		/*                                                                    */
		/* Section for computation of temperature change of pipe. */
		/* Coding is an approximation splitting pipe into 10 subsections */
		/* with individual temperature. */
		/*                                                                    */
		/* J= Number of pipe section */
		/*                                                                    */
		/* Fluid properties (viscosity ...) are considered to be */
		/* constant over entire pipe, same as Nusselt number and thus */
		/* heat transfer coefficient Alfa. */
		/*                                                                    */
		/* Computation of the heat transfer numbers, */
		/* XI,Prandtl-Zahl, Nusselt, ALFA. */
		/* please refer to [1] section 3.3.3.2, Eq..(3.1) ff */
		/*                                                                    */
		/* Change: Approximation of Laval number at pipe outlet to be same */
		/* as L1 at pipe inlet port. */
		/*                                                                    */
		/* Computation of heatflow from pipe to fluid */
		/* for each pipe of the 10 pipe elements. */
		/*                                                                    */
		/**********************************************************************/
		for (J = 0; J < PARTS; J++) {
			qHFlow[J] = alfa * 3.1415 * innerDiameter * length
					* (temperatures[J] - tstatin) / PARTS;
			Q = qHFlow[J] * tStepSize;

			/******************************************************************/
			/*                                                                */
			/* Computation of delta T for fluid and new fluid temperature */
			/* for each pipe element */
			/*                                                                */
			/******************************************************************/
			DTF = qHFlow[J] / (mfin * CP);
			tstatin = tstatin + DTF;

			/******************************************************************/
			/*                                                                */
			/* Computation of delta T of each pipe section */
			/* and computation of new pipe temp. for each section */
			/*                                                                */
			/******************************************************************/
			DTB = Q / (massPElem * specificHeatCapacity);
			temperatures[J] = temperatures[J] - DTB;
		}
		return 0;
	}

	public FluidPort backIterStep(FluidPort outputPort) {
		if (outputPort.getBoundaryPressure() >= 0.0) {
			LOG.error("Pressure request on output port cannot be handled!");
		}
		if (outputPort.getBoundaryTemperature() >= 0.0) {
			LOG.error("Temp. request on output port cannot be handled!");
		}
		return BoundaryUtils.createBoundaryPort(outputPort);
	}


	public FluidPort createOutputPort(String fluid) {
		FluidPort outputPort = new FluidPort();
		outputPort.setFluid(fluid);
		outputPort.setPressure(pout);
		outputPort.setTemperature(tout);
		outputPort.setMassflow(mfin);
		return outputPort;
	}
	

	
	// -----------------------------------------------------------------------------------
	// Methods added for JMX monitoring and setting initial properties via CDI
	// Extensions

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
	public double getSurfaceRoughness() {
		return surfaceRoughness;
	}

	public void setSurfaceRoughness(double surfaceRoughness) {
		this.surfaceRoughness = surfaceRoughness;
	}

	@ManagedAttribute
	public double[] getTemperatures() {
		return temperatures;
	}

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
