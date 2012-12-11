/*
 * ScStructure.java
 *
 *  Model definition for a very simple spacecraft structure model.
 *
 *  Created on 20. February 2009
 *
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-20
 *      File created  C. Ziemke:
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 *
 *  2009-08
 *      Minor cleanups for integration with engine model and others.
 *      J. Eickhoff
 *
 *  2009-11
 *      Added coordinate transformations to ECEF and conversion to
 *      longitude / latitude / altitude coordinates
 *      based on Java Astrodynamics Toolkit.
 *      I. Kossev
 *
 *  2009-12
 *      Update modifying engine thrust vector always being interpreted to
 *      be aligned as tangent to the orbit vector. This allows
 *      simulation of e.g. Hohman transfers already w/o a complete
 *      spacecraft attitude dynamics model.
 *      I. Kossev
 *
 *  2009-12
 *      Eliminated bug in getVelocityECEF.
 *      J. Eickhoff
 *
 *  2010-01
 *      Upgraded to comprise functionality for simplified attitude quaternion computation.
 *      This feature is used in conjunction to coupling OSK to Celestia for 3D 
 *      visualization of spacecraft in-orbit motion.
 *      J. Eickhoff
 *
 *  2010-02
 *      Upgraded to provide S/C position data for 3D visualization in Celestia.
 *      J. Eickhoff
 *
 *  2010-02
 *      Upgraded to provide S/C attitude data for 3D visualization in Celestia.
 *      I. Kossev
 *
 *  2010-03
 *      Experimental debugging of coordinate system differences btw. Celestia
 *      and JAT. Quaternion conversion computations added accordingly.
 *      J. Eickhoff
 *
 *  2010-04
 *      Integration of SimVisThread for each ScStructure instance added.
 *      J. Eickhoff
 *
 *  2010-11
 *      Integration of coordinate transformation bugfix and reintegration 
 *      w. Celestia.
 *      J. Eickhoff, I. Kossev
 *
 *  2010-12
 *      Fix of ECI->ECEF transformation problem with JAT (bug or misuse
 *      due to unclear documentation).
 *      M. Fritz, O. Zeile, J. Eickhoff
 *
 *  2011-01
 *      Implemented:
 *      Altitude handover to engine via provider/subscriber mechanism 
 *      instead of dedicated port class.
 *      Thrust vector handover to ScStructure via same mechanism.
 *      Diverse minor cleanups.
 *      J. Eickhoff
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
 */

package org.osk.models.structure;

import javax.inject.Inject;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.errors.OskException;
import org.osk.events.PVCoordinates;
import org.osk.models.BaseModel;
import org.osk.time.TimeHandler;
import org.slf4j.Logger;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for a point mass.
 * 
 * @author C. Ziemke
 * @author A. Brandt
 * @author J. Eickhoff
 * @author I. Kossev
 * @author M. Fritz
 * @author P. Pita
 */

public class ScStructure extends BaseModel {

	@Inject Logger LOG;
	@Inject TimeHandler timeHandler;

	/** Gravity acceleration imposed onto structure. */
	private Vector3D gravityAccel; 
	/** Structure mass. */
	private double scMass;

	/** Structure SCVelocity in ECI frame. */
	private Vector3D scVelocityECI; //= new Vector3D();
	/** Structure SCPosition in ECI frame. */
	private Vector3D scPositionECI; //= new Vector3D();
	/** scPosition of previous timestep. */
	private Vector3D scPositionECI_prev; // = new Vector3D();
	/** scVelocity of previous timestep. */
	private Vector3D scVelocityECI_prev; // = new Vector3D();

	private double scVelocityX, scVelocityY, scVelocityZ;
	private double scPositionX, scPositionY, scPositionZ;

	/** Direction vector (unitized) of engine thrust in ECI frame. */
	
	private static final String TYPE = "ScStructure";
	private static final String SOLVER = "none";

	/*----------------------------------------------------------------------
	Note! The variable(s)
	    tVec[]
	is(are) used in subsequent code lines.
	Please assure that the according variable(s) is(are) provided to this 
	model via the provider/subscriber mechanism by specifying an according 
	subscription entry in the simulation input file.
	------------------------------------------------------------------------
	Note! The variable(s)
	    scPosAlt
	    scPositionECI[]
	is(are) computed for use by other models in subsequent code lines.
	Please assure that the according variable(s) is(are) handed over to
	subscribers by specifying an according provision entry in the simulation 
	input file.
	----------------------------------------------------------------------*/

	public ScStructure() {
		super(TYPE, SOLVER);
	}


	public void init(String name) {
		this.name = name;
		scPositionECI_prev = scPositionECI;
		scVelocityECI_prev = scVelocityECI;
	}

	public PVCoordinates timeStep(Vector3D thrust) throws OskException {

		// FIXME I wish the semantics of the vectorial calculus was better expressed in the code ...
		// See if the Frames concept from OREKIT can help
		double thrustMag = thrust.getNorm(); 
		Vector3D thrustVecECI = scPositionECI.subtract(scPositionECI_prev)
				.normalize().scalarMultiply(thrustMag);
		LOG.info("thrustMag:  '{}' ", thrustMag);

		// the actual acceleration is the composition of the gravity plus the rocket
		// thrust done in ECI coordinates
		Vector3D totalAcc = gravityAccel.add(1.0 / scMass, thrustVecECI); 
		
		/* Calculation of the new SC velocity.  v = v0 + a * dt */
		double tStepSize = timeHandler.getStepSizeAsDouble();
		// newVelocityECI = scVelocityECI + a * dt
		Vector3D newVelocityECI = scVelocityECI.add(tStepSize, totalAcc);
		Vector3D newPositionECI = scPositionECI.add(tStepSize, newVelocityECI);

		/* Storage of old SC values */
		scPositionECI_prev = scPositionECI;
		scVelocityECI_prev = scVelocityECI;
		scPositionECI = newPositionECI;
		scVelocityECI = newVelocityECI;

		// Up to here, we have calculated S/C position and velocity
		// in ECI coordinates. This method should be just considered done.
		// Those methods involving calculations
		// in other reference frames (EDEF) are moved to other classes.

		return new PVCoordinates(newPositionECI, newVelocityECI);
	}

	public Vector3D getGravity() {
		return gravityAccel;
	}
	
	public void setGravity(Vector3D gravity) {
			gravityAccel = gravity;
		
	}

	// JMX

	@ManagedAttribute
	public double getScMass() {
		return scMass;
	}

	public void setScMass(double scMass) {
		this.scMass = scMass;
	}

	@ManagedAttribute
	public double getScVelocityX() {
		return scVelocityX;
	}

	public void setScVelocityX(double scVelocityX) {
		this.scVelocityX = scVelocityX;
	}

	@ManagedAttribute
	public double getScVelocityY() {
		return scVelocityY;
	}

	public void setScVelocityY(double scVelocityY) {
		this.scVelocityY = scVelocityY;
	}

	@ManagedAttribute
	public double getScVelocityZ() {
		return scVelocityZ;
	}

	public void setScVelocityZ(double scVelocityZ) {
		this.scVelocityZ = scVelocityZ;
	}

	@ManagedAttribute
	public double getScPositionX() {
		return scPositionX;
	}

	public void setScPositionX(double scPositionX) {
		this.scPositionX = scPositionX;
	}

	@ManagedAttribute
	public double getScPositionY() {
		return scPositionY;
	}

	public void setScPositionY(double scPositionY) {
		this.scPositionY = scPositionY;
	}

	@ManagedAttribute
	public double getScPositionZ() {
		return scPositionZ;
	}

	public void setScPositionZ(double scPositionZ) {
		this.scPositionZ = scPositionZ;
	}

	@ManagedAttribute
	public Vector3D getScVelocityECI() {
		return scVelocityECI;
	}

	public void setScVelocityECI(Vector3D scVelocityECI) {
		this.scVelocityECI = scVelocityECI;
	}

	@ManagedAttribute
	public Vector3D getScPositionECI() {
		return scPositionECI;
	}

	public void setScPositionECI(Vector3D scPositionECI) {
		this.scPositionECI = scPositionECI;
	}

}