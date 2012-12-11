/*
/*-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *-----------------------------------------------------------------------------
 */
package org.osk.models.environment;

import jat.forces.GravityModel;
import jat.matvec.data.Matrix;
import jat.matvec.data.VectorN;
import jat.spacetime.EarthRef;
import jat.spacetime.Time;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.TimeHandler;
import org.osk.errors.OskException;
import org.osk.models.BaseModel;
import org.slf4j.Logger;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 *
 * @author M. Kobald
 * @author A. Bohr
 * @author A. Brandt
 * @author J. Eickhoff
 */


public class OSKGravityModel extends BaseModel {
	@Inject Logger LOG;
    @Inject TimeHandler timeHandler;
    /** Order of spherical harmonic. */
     private int order = 8; // hardcoded for now 
    /** Degree of spherical harmonic. */
     private int degree = 8; // hardcoded for now 
    /** Structure SCPosition in ECI frame. */
     private Vector3D scPositionECI; // = new double[3];
    /** JAT Earth reference frame. */
    private EarthRef earthReference;
    /** JAT Earth gravity model. */
    private GravityModel gravityModel;
    /** OSK SRT time in converted format. */
    Time convertedMissionTime;
    
    private static final String TYPE = "OSKGravity1";
    private static final String SOLVER = "none";
 
    public OSKGravityModel() {
        super(TYPE, SOLVER);
    }

    public void init() throws OskException {
        convertedMissionTime = new Time();
        earthReference = new EarthRef(convertedMissionTime);
        // Maybe one day this file is read as resource from classpath 
        File path = new File("src/main/resources/earthGravity/JGM3.grv");

        String pathString;
        try {
            pathString = path.getCanonicalPath();
            gravityModel = new GravityModel(order, degree, pathString);
        } catch (IOException ex) {
            throw new OskException(new DummyLocalizable(ex.getMessage()));
        }
    }


    public Vector3D computeEarthGravity() {
    	/* JAT Class of S/C position vector in ECI. An event has supplied the scPositionECI */
    	VectorN  scPositionVector = new VectorN(3);
    	scPositionVector.set(0, scPositionECI.getX());
    	scPositionVector.set(1, scPositionECI.getY());
    	scPositionVector.set(2, scPositionECI.getZ());

    	convertedMissionTime.update(timeHandler.getSimulatedMissionTimeAsDouble());
    	/* ECI to ECEF conversion matrix of JAT. */
    	Matrix eci2ECEFMatrix = earthReference.eci2ecef(convertedMissionTime);
    	return new Vector3D(gravityModel.gravity(scPositionVector, eci2ECEFMatrix).getArray());        
    }

    //----------------------------------------
    // Methods added for JMX monitoring	

	@ManagedAttribute
	public Vector3D getScPositionECI() {
		return scPositionECI;
	}


	public void setScPositionECI(Vector3D scPositionECI) {
		this.scPositionECI = scPositionECI;
	}

	@ManagedAttribute
	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}

	@ManagedAttribute
	public int getDegree() {
		return degree;
	}


	public void setDegree(int degree) {
		this.degree = degree;
	}
	
}
