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

	private static final String TYPE = "ScStructure";
	private static final String SOLVER = "none";

	public ScStructure() {
		super(TYPE, SOLVER);
	}

	public void init(String name) {
		this.name = name;
		scPositionECI_prev = scPositionECI;
	}

	public PVCoordinates calculateECICoordinates(Vector3D thrust) throws OskException {
		// FIXME See if the Frames concept from OREKIT can be used 
		final double thrustMag = thrust.getNorm(); 
		final Vector3D thrustVecECI = scPositionECI.subtract(scPositionECI_prev)
				.normalize().scalarMultiply(thrustMag);
		LOG.info("thrustMag:  '{}' ", thrustMag);

		// the actual acceleration is the composition of the gravity plus the rocket
		// thrust done in ECI coordinates
		final Vector3D totalAcc = gravityAccel.add(1.0/scMass, thrustVecECI); 
		
		/* Calculation of the new SC velocity.  v = v0 + a * dt */
		final double tStepSize = timeHandler.getStepSizeAsDouble();

		final Vector3D newVelocityECI = scVelocityECI.add(tStepSize, totalAcc);
		final Vector3D newPositionECI = scPositionECI.add(tStepSize, newVelocityECI);

		/* Storage of old SC values */
		scPositionECI_prev = scPositionECI;
		scPositionECI = newPositionECI;
		scVelocityECI = newVelocityECI;

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