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
 
package org.opensimkit.models.structure;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.opensimkit.BaseModel;
import org.opensimkit.TimeHandler;
import org.opensimkit.events.D4Value;
import org.opensimkit.events.ECI;
import org.opensimkit.events.Gravity;
import org.opensimkit.events.ScPV;
import org.opensimkit.events.Thrust;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a point mass.
 *
 * @author C. Ziemke
 * @author A. Brandt
 * @author J. Eickhoff
 * @author I. Kossev
 * @author M. Fritz
 * @version 4.0
 * @since 2.8.0
 */

public class ScStructure extends BaseModel {

	/** Logger instance for the ScStructure. */
    private static final Logger LOG = LoggerFactory.getLogger(ScStructure.class);
    /** Gravity acceleration imposed onto structure. */
    @Manipulatable private double[] gravityAccel = new double[4];
    /** Structure mass. */
    @Manipulatable private double scMass;
    
    /** Structure SCVelocity in ECI frame. */
    @Manipulatable private double[] scVelocityECI = new double[3];
    /** Structure SCPosition in ECI frame. */
    @Manipulatable private double[] scPositionECI = new double[3];
    /**Thrust: Value and direction vector in SC body frame. Vector components in [N] */
    @Manipulatable private final double[] tVec = new double[4];


    /** scPosition of previous timestep. */
    private double[] scPositionECI_prev= new double[3];
    /** scVelocity of previous timestep. */
    private double[] scVelocityECI_prev = new double[3];

    @Readable private double scVelocityX, scVelocityY, scVelocityZ;
    @Readable private double scPositionX, scPositionY, scPositionZ;

    /** Time handler object. */
    @Inject
    private TimeHandler timeHandler;
    /** OSK SRT time in converted format. */
    
    /** Magnitude of engine thrust. */
    private double thrustMag;
    /** Direction vector (unitized) of engine thrust in ECI frame. */
    private double[] thrustVecECI = new double[3];
    
    private static final String TYPE = "ScStructure";
    private static final String SOLVER = "none";
    private static final double MAXTSTEP = 10.0;
    private static final double MINTSTEP = 0.001;
    private static final int TIMESTEP = 1;
    private static final int REGULSTEP = 0;

    // The ECI annotiation implies that the events are related to ECI coord.
    @Inject @ECI Event<ScPV> scPVEvent;

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

    /**
     * Creates a new instance of the S/C structure.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
    public ScStructure(final String name) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }


    /**
    * The initialization of the Component takes place in this method. It is
    * called after the creation of the instance and the loading of its default
    * values so that derived variables can be calculated after loading or
    * re-calculated after the change of a manipulatable variable (but in this
    * case the init method must be called manually!).
    */
    @Override
    public void init() {
        /* Computation of derived initialization parameters. */
        thrustMag = 0.0;
        tVec[0] = 0;
        tVec[1] = 0;
        tVec[2] = 0;
        tVec[3] = 0;
        thrustVecECI[0] = 0;
        thrustVecECI[1] = 0;
        thrustVecECI[2] = 0;

        for(int i = 0; i < 3 ; i++){
            scPositionECI_prev[i] = scPositionECI[i];
            scVelocityECI_prev[i] = scVelocityECI[i];
        }
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.debug("% {} TimeStep-Computation", name);        
        LOG.debug("gravityAccel[0]:  '{}' ", gravityAccel[0]);
        LOG.debug("gravityAccel[1]:  '{}' ", gravityAccel[1]);
        LOG.debug("gravityAccel[2]:  '{}' ", gravityAccel[2]);
        LOG.debug("gravityAccel[3]:  '{}' ", gravityAccel[3]);
                
        thrustVecECI = this.getNormVector(this.getVecDiff(scPositionECI, scPositionECI_prev));
        thrustMag = tVec[0];
        LOG.debug("thrustMag:  '{}' ", thrustMag);

        double scMass_inverted = 0.0;
        if (scMass >= 0.0) {
            scMass_inverted = 1.0 / scMass;
        } else {
            LOG.error("S/C mass is negative({})!", scMass);
        }

        double[] totalAcc = new double[3];
        for (int i = 0; i < 3; i++) {
            if (thrustMag == 0.0) {
                totalAcc[i] = gravityAccel[i+1] * gravityAccel[0];
            } else {
                totalAcc[i] = gravityAccel[i+1] * gravityAccel[0] + thrustVecECI[i]
                        * thrustMag * scMass_inverted;
            }
            LOG.debug("totalAcc[i]:  '{}' ", totalAcc[i]);
        }
        double[] result = new double[3];

        for (int i = 0; i < 3; i++) {
            scVelocityECI_prev[i] = scVelocityECI[i];
        }
        
        /* Calculation of the new SC velocity. */
        result = integratorTimeStep(time, tStepSize, scVelocityECI, totalAcc);
        for (int i = 0; i < 3; i++) {
            scVelocityECI[i] = result[i];
        }

        /* Storage of old SC position. */
        for (int i = 0; i < 3; i++) {
            scPositionECI_prev[i] = scPositionECI[i];
        }

        /* Calculation of the new SC position. */
        result = integratorTimeStep(time, tStepSize, scPositionECI, scVelocityECI);
        for (int i = 0; i < 3; i++) {
            scPositionECI[i] = result[i];
        }
        
        // Up to here, we have calculated S/C position and velocity
        // in ECI coordinates. This method should be just considered done.
        // Those methods involving calculations
        // in other reference frames (EDEF) are moved to other classes.
        
        scPVEvent.fire(new ScPV(scPositionECI, scVelocityECI));
        
        return 0;
    }



    /**
     * Calculates and returns the left hand side of the model's ODE.
     */
    public double[] getValue(final double time, final double[] x,
            final double[] inp) {
        double[] result = new double[3];

        result[0] = inp[0];
        result[1] = inp[1];
        result[2] = inp[2];

        return result;
    }



    /**
    * Runge Kutta Integrator for the model's ODE.
    */
    public double[] integratorTimeStep(final double t, final double dt,
            final double[] state, final double[] input) {
        int i;
        int resultSize = state.length;
        double[] result = new double[resultSize];
        // temporary
        double[] k1 = new double[resultSize];
        double[] k2 = new double[resultSize];
        double[] k3 = new double[resultSize];
        double[] k4 = new double[resultSize];
        ////double a1= 1./6., a2 = 1./3., a3 = 1./3., a4= 1./6.;

        k1 = getValue(t, state, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + k1[i] * dt / 2.;
        }

        k2 = getValue(t + dt / 2., result, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + k2[i] * dt / 2.;
        }

        k3 = getValue(t + dt / 2., result, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + k3[i] * dt;
        }

        k4 = getValue(t + dt, result, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + 1. / 6. * dt
                    * (k1[i] + 2. * k2[i] + 2. * k3[i] + k4[i]);
        }

        return result;
    }



    /**
     * Returns the norm vector of a vector.
     * @param vector
     * @return
     */
    public double[] getNormVector(final double[] vector) {
        double norm = Math.sqrt(getScalarProduct(vector, vector));
        double[] result = new double[3];

        if (norm <= 0.0000001){
            for(int i=0; i<3; i++){
                result[i] = 0.0;
            }
        } else {
            for (int i = 0; i < 3; i++) {
                result[i] = vector[i] / norm;
            }
        }
        return result;
    }



    /**
     * Returns the scalar product of two vectors.
     * @param vector_a
     * @param vector_b
     * @return
     */
    public double getScalarProduct(final double[] vector_a,
        final double[] vector_b) {
        double tmp = 0.0;
        for (int i = 0; i < 3; i++) {
            tmp = tmp + vector_a[i] * vector_b[i];
        }
        return tmp;
    }


    public double[] getVecDiff(double[] vector2, double[] vector1) throws ArithmeticException {
        if (vector2.length != vector1.length) {
            throw new ArithmeticException("Dimensions do not fit.");
        }
        if ((vector1.length == 0) || (vector2.length == 0)) {
            throw new  ArithmeticException("Dimension is zero.");
        }
        double[] result = new double[vector2.length];
        for (int i = 0; i < 3; i++) {
            result[i] = vector2[i]-vector1[i];
        }
        return result;
    }



    
    // The following method allows to multiply two 3x3 matrices
    public double[][] matrixmult(double[][] matrix01, double[][] matrix02) {
        double[][] resmatrix = {{0,0,0},{0,0,0},{0,0,0}};
        for(int i=0; i<matrix01.length; i++) {
            for(int j=0; j<matrix01.length; j++) {
                resmatrix[i][j] = matrix01[i][0]*matrix02[0][j] + matrix01[i][1]*matrix02[1][j] + matrix01[i][2]*matrix02[2][j];
            }
        }
        return resmatrix;
    }
    
    public double[] matrvectmult(double[][] matrix, double[] vector) {
        double[] resvector = {0,0,0};
        for(int i=0; i<matrix.length; i++) {
            resvector[i] = matrix[i][0] * vector[0] + matrix[i][1] * vector[1] + matrix[i][2] * vector[2];
        }
        return resvector;
    }


	public double getScMass() {
		return scMass;
	}


	public void setScMass(double scMass) {
		this.scMass = scMass;
	}


	public double getScVelocityX() {
		return scVelocityX;
	}


	public void setScVelocityX(double scVelocityX) {
		this.scVelocityX = scVelocityX;
	}


	public double getScVelocityY() {
		return scVelocityY;
	}


	public void setScVelocityY(double scVelocityY) {
		this.scVelocityY = scVelocityY;
	}


	public double getScVelocityZ() {
		return scVelocityZ;
	}


	public void setScVelocityZ(double scVelocityZ) {
		this.scVelocityZ = scVelocityZ;
	}


	public double getScPositionX() {
		return scPositionX;
	}


	public void setScPositionX(double scPositionX) {
		this.scPositionX = scPositionX;
	}


	public double getScPositionY() {
		return scPositionY;
	}


	public void setScPositionY(double scPositionY) {
		this.scPositionY = scPositionY;
	}


	public double getScPositionZ() {
		return scPositionZ;
	}


	public void setScPositionZ(double scPositionZ) {
		this.scPositionZ = scPositionZ;
	}
	
    public double[] getScVelocityECI() {
		return scVelocityECI;
	}

	public void setScVelocityECI(double[] scVelocityECI) {
		this.scVelocityECI = scVelocityECI;
	}

	public double[] getScPositionECI() {
		return scPositionECI;
	}

	public void setScPositionECI(double[] scPositionECI) {
		this.scPositionECI = scPositionECI;
	}

	public void thrustHandler(@Observes @Thrust D4Value thrust) {
		double[] t = thrust.getValue();
		assert t.length == 4;
		for (int i=0; i<4; i++) {
			tVec[i] = t[i];
		}		
	}

	public void gravityHandler(@Observes @Gravity D4Value gravity) {
		double[] g = gravity.getValue();
		assert g.length == 4;
		for (int i=0; i<4; i++) {
			gravityAccel[i] = g[i];
		}		
	}
	
}